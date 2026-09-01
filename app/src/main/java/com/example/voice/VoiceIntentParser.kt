package com.example.voice

import com.example.data.model.Person
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import java.util.Locale

object VoiceIntentParser {

    /**
     * Parse raw spoken voice input against existing known contacts.
     */
    fun parse(rawText: String, contacts: List<Person>): VoiceIntentResult {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return VoiceIntentResult.Unknown(
                rawSpeech = rawText,
                message = "Please speak a transaction, for example: 'I gave 500 to Ramesh' or 'సుల్తాన్ 1000 తిరిగి ఇచ్చాడు'",
                teluguMessage = "దయచేసి మాట్లాడండి, ఉదాహరణకు: 'రమేష్కి 500 ఇచ్చాను' లేదా 'సుల్తాన్ 1000 తిరిగి ఇచ్చాడు'"
            )
        }

        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Balance Query first
        val isBalanceQuery = checkIsBalanceQuery(lower, trimmed)
        val matchedContact = findContact(trimmed, contacts)

        if (isBalanceQuery) {
            if (matchedContact != null) {
                return VoiceIntentResult.BalanceCheck(
                    person = matchedContact,
                    rawSpeech = trimmed,
                    queryDescription = "Checking balance for ${matchedContact.name}"
                )
            } else {
                val candidateName = extractCandidateName(trimmed, contacts)
                return if (candidateName.isNotBlank()) {
                    VoiceIntentResult.PromptNewPerson(
                        candidateName = candidateName,
                        pendingAmount = null,
                        pendingType = null,
                        pendingDirection = null,
                        rawSpeech = trimmed,
                        question = "New person — add $candidateName to check balance?",
                        teluguQuestion = "కొత్త వ్యక్తి — బ్యాలెన్స్ చూడటానికి $candidateName ని చేర్చమంటారా?"
                    )
                } else {
                    VoiceIntentResult.Unknown(
                        rawSpeech = trimmed,
                        message = "Could not identify person for balance check. Try saying 'Show Ramesh balance'",
                        teluguMessage = "ఎవరి బ్యాలెన్స్ చూడాలో అర్థం కాలేదు. 'రమేష్ బాకీ ఎంత?' అని చెప్పండి"
                    )
                }
            }
        }

        // 2. Extract Amount
        val amount = extractAmount(trimmed)

        // 3. Match Contact or Candidate Name
        val person = matchedContact
        val candidateName = if (person == null) extractCandidateName(trimmed, contacts) else ""

        if (amount == null || amount <= 0.0) {
            if (person != null) {
                return VoiceIntentResult.Unknown(
                    rawSpeech = trimmed,
                    message = "Found ${person.name}, but amount was not recognized. Please specify amount (e.g. 500).",
                    teluguMessage = "${person.name} గుర్తించబడింది, కానీ మొత్తం అర్థం కాలేదు. దయచేసి రూ. 500 వంటి మొత్తం చెప్పండి."
                )
            }
            return VoiceIntentResult.Unknown(
                rawSpeech = trimmed,
                message = "Could not understand the transaction. Please specify person and amount.",
                teluguMessage = "లావాదేవీ అర్థం కాలేదు. దయచేసి పేరు మరియు మొత్తం చెప్పండి."
            )
        }

        // 4. Detect Intent Direction / Action Verb
        val detectedAction = detectAction(lower, trimmed)

        // 5. Handle Intent outcomes
        when (detectedAction) {
            ActionDetection.LENT -> {
                // Shopkeeper gave money -> Person owes shopkeeper
                if (person != null) {
                    return VoiceIntentResult.ExecuteTransaction(
                        person = person,
                        amount = amount,
                        type = TransactionType.LENT,
                        direction = TransactionDirection.GAVE,
                        rawSpeech = trimmed,
                        explanation = "Gave ₹${formatAmount(amount)} to ${person.name} (Lent / Credit)",
                        teluguExplanation = "${person.name} కి ₹${formatAmount(amount)} ఇచ్చారు (ఖాతా / అప్పు)"
                    )
                } else if (candidateName.isNotBlank()) {
                    return VoiceIntentResult.PromptNewPerson(
                        candidateName = candidateName,
                        pendingAmount = amount,
                        pendingType = TransactionType.LENT,
                        pendingDirection = TransactionDirection.GAVE,
                        rawSpeech = trimmed,
                        question = "New person — add $candidateName and log ₹${formatAmount(amount)} given?",
                        teluguQuestion = "కొత్త వ్యక్తి — $candidateName ని చేర్చి ₹${formatAmount(amount)} ఇచ్చినట్లు నమోదు చేయమంటారా?"
                    )
                }
            }

            ActionDetection.BORROWED -> {
                // Shopkeeper took money from person -> Shopkeeper owes person
                if (person != null) {
                    return VoiceIntentResult.ExecuteTransaction(
                        person = person,
                        amount = amount,
                        type = TransactionType.BORROWED,
                        direction = TransactionDirection.RECEIVED,
                        rawSpeech = trimmed,
                        explanation = "Took ₹${formatAmount(amount)} from ${person.name} (Borrowed)",
                        teluguExplanation = "${person.name} దగ్గర ₹${formatAmount(amount)} తీసుకున్నారు"
                    )
                } else if (candidateName.isNotBlank()) {
                    return VoiceIntentResult.PromptNewPerson(
                        candidateName = candidateName,
                        pendingAmount = amount,
                        pendingType = TransactionType.BORROWED,
                        pendingDirection = TransactionDirection.RECEIVED,
                        rawSpeech = trimmed,
                        question = "New person — add $candidateName and log ₹${formatAmount(amount)} borrowed?",
                        teluguQuestion = "కొత్త వ్యక్తి — $candidateName ని చేర్చి ₹${formatAmount(amount)} తీసుకున్నట్లు నమోదు చేయమంటారా?"
                    )
                }
            }

            ActionDetection.REPAYMENT -> {
                // Person paid back money to shopkeeper
                if (person != null) {
                    return VoiceIntentResult.ExecuteTransaction(
                        person = person,
                        amount = amount,
                        type = TransactionType.REPAYMENT,
                        direction = TransactionDirection.RECEIVED,
                        rawSpeech = trimmed,
                        explanation = "Received ₹${formatAmount(amount)} repayment from ${person.name}",
                        teluguExplanation = "${person.name} నుండి ₹${formatAmount(amount)} జమ/తిరిగి వచ్చింది"
                    )
                } else if (candidateName.isNotBlank()) {
                    return VoiceIntentResult.PromptNewPerson(
                        candidateName = candidateName,
                        pendingAmount = amount,
                        pendingType = TransactionType.REPAYMENT,
                        pendingDirection = TransactionDirection.RECEIVED,
                        rawSpeech = trimmed,
                        question = "New person — add $candidateName and record ₹${formatAmount(amount)} repayment?",
                        teluguQuestion = "కొత్త వ్యక్తి — $candidateName ని చేర్చి ₹${formatAmount(amount)} తిరిగి ఇచ్చినట్లు నమోదు చేయమంటారా?"
                    )
                }
            }

            ActionDetection.AMBIGUOUS -> {
                val targetPerson = person ?: Person(name = candidateName.ifBlank { "Customer" })
                return VoiceIntentResult.AmbiguousIntent(
                    person = targetPerson,
                    amount = amount,
                    rawSpeech = trimmed,
                    question = "Did ${targetPerson.name} take ₹${formatAmount(amount)} or repay ₹${formatAmount(amount)}?",
                    teluguQuestion = "${targetPerson.name} ₹${formatAmount(amount)} తీసుకున్నారా లేక తిరిగి ఇచ్చారా?",
                    optionA = PendingOption(
                        label = "Gave to ${targetPerson.name} (Lent)",
                        teluguLabel = "${targetPerson.name} కి ఇచ్చాను",
                        type = TransactionType.LENT,
                        direction = TransactionDirection.GAVE
                    ),
                    optionB = PendingOption(
                        label = "Received Repayment (Paid Back)",
                        teluguLabel = "తిరిగి ఇచ్చాడు / వచ్చింది",
                        type = TransactionType.REPAYMENT,
                        direction = TransactionDirection.RECEIVED
                    )
                )
            }
        }

        // Default fallback
        val defaultPerson = person ?: Person(name = candidateName.ifBlank { "Unknown" })
        return VoiceIntentResult.AmbiguousIntent(
            person = defaultPerson,
            amount = amount,
            rawSpeech = trimmed,
            question = "Did ${defaultPerson.name} take ₹${formatAmount(amount)} or repay ₹${formatAmount(amount)}?",
            teluguQuestion = "${defaultPerson.name} ₹${formatAmount(amount)} తీసుకున్నారా లేక తిరిగి ఇచ్చారా?",
            optionA = PendingOption(
                label = "Gave to ${defaultPerson.name} (Lent)",
                teluguLabel = "${defaultPerson.name} కి ఇచ్చాను",
                type = TransactionType.LENT,
                direction = TransactionDirection.GAVE
            ),
            optionB = PendingOption(
                label = "Received Repayment (Paid Back)",
                teluguLabel = "తిరిగి ఇచ్చాడు / వచ్చింది",
                type = TransactionType.REPAYMENT,
                direction = TransactionDirection.RECEIVED
            )
        )
    }

    private enum class ActionDetection {
        LENT, BORROWED, REPAYMENT, AMBIGUOUS
    }

    private fun detectAction(lower: String, raw: String): ActionDetection {
        // 1. Check Repayment indicators first
        val isRepay = lower.contains("paid back") ||
                lower.contains("pay back") ||
                lower.contains("returned") ||
                lower.contains("gave back") ||
                lower.contains("settled") ||
                lower.contains("repaid") ||
                lower.contains("received") ||
                lower.contains("తిరిగి") ||
                lower.contains("చెల్లించాడు") ||
                lower.contains("చెల్లించింది") ||
                lower.contains("కట్టాడు") ||
                lower.contains("వచ్చింది") ||
                lower.contains("రిటర్న్") ||
                lower.contains("tirigi") ||
                lower.contains("chellinchaadu") ||
                lower.contains("kattaadu")

        if (isRepay) {
            return ActionDetection.REPAYMENT
        }

        // 2. Check Borrowed (Owner took from person / Person gave to owner)
        // User prompt: "I took 2000 from Sulthan" / "Sulthan gave me 2000" / "సుల్తాన్ దగ్గర 2000 తీసుకున్నాను"
        val isBorrowed = lower.contains("i took") ||
                lower.contains("took from") ||
                lower.contains("gave me") ||
                lower.contains("borrowed from") ||
                lower.contains("దగ్గర") && (lower.contains("తీసుకున్నాను") || lower.contains("తీసుకున్నాము")) ||
                lower.contains("తీసుకున్నాను") ||
                lower.contains("నాకు ఇచ్చాడు") ||
                lower.contains("daggara") && lower.contains("teesukunnanu") ||
                lower.contains("teesukunnanu")

        // 3. Check Lent (Owner gave to person / Person took from owner)
        // User prompt: "I gave 500 to Ramesh" / "Ramesh took 500 from me" / "రమేష్కి 500 ఇచ్చాను"
        val isLent = lower.contains("i gave") ||
                lower.contains("gave to") ||
                lower.contains("gave") ||
                lower.contains("lent to") ||
                lower.contains("lent") ||
                lower.contains("took from me") ||
                lower.contains("తీసుకున్నాడు") ||
                lower.contains("తీసుకుంది") ||
                lower.contains("ఇచ్చాను") ||
                lower.contains("ఇచ్చాము") ||
                lower.contains("అప్పు ఇచ్చాను") ||
                lower.contains("icchanu") ||
                lower.contains("teesukunnadu")

        return when {
            isBorrowed && !isLent -> ActionDetection.BORROWED
            isLent && !isBorrowed -> ActionDetection.LENT
            isBorrowed && isLent -> ActionDetection.AMBIGUOUS
            else -> ActionDetection.AMBIGUOUS
        }
    }

    private fun checkIsBalanceQuery(lower: String, raw: String): Boolean {
        return lower.contains("how much") ||
                lower.contains("owe") ||
                lower.contains("balance") ||
                lower.contains("show") && lower.contains("balance") ||
                lower.contains("check") && lower.contains("balance") ||
                lower.contains("ఎంత బాకీ") ||
                lower.contains("బాకీ ఎంత") ||
                lower.contains("ఎంత ఇవ్వాలి") ||
                lower.contains("ఎంత రావాలి") ||
                lower.contains("బాకీ") ||
                lower.contains("బ్యాలెన్స్") ||
                lower.contains("baaki entha") ||
                lower.contains("entha baaki") ||
                lower.contains("balance")
    }

    fun extractAmount(text: String): Double? {
        // Regex for numeric digits (e.g. ₹2,000, 2000, 500.50, Rs 1500)
        val cleaned = text.replace(",", "").replace("₹", " ").replace("rs.", " ", ignoreCase = true).replace("rs", " ", ignoreCase = true)
        val regex = Regex("""\b\d+(?:\.\d+)?\b""")
        val match = regex.find(cleaned)
        if (match != null) {
            return match.value.toDoubleOrNull()
        }

        // Fallback: Check for common spoken words in English and Telugu
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("రెండు వేలు") || lower.contains("two thousand") || lower.contains("2 thousand") -> 2000.0
            lower.contains("మూడు వేలు") || lower.contains("three thousand") -> 3000.0
            lower.contains("నాలుగు వేలు") || lower.contains("four thousand") -> 4000.0
            lower.contains("ఐదు వేలు") || lower.contains("five thousand") -> 5000.0
            lower.contains("పది వేలు") || lower.contains("ten thousand") -> 10000.0
            lower.contains("వెయ్యి") || lower.contains("one thousand") || lower.contains("a thousand") -> 1000.0
            lower.contains("ఐదు వందలు") || lower.contains("five hundred") -> 500.0
            lower.contains("రెండు వందలు") || lower.contains("two hundred") -> 200.0
            lower.contains("వంద") || lower.contains("one hundred") || lower.contains("a hundred") -> 100.0
            lower.contains("యాభై") || lower.contains("fifty") -> 50.0
            else -> null
        }
    }

    private fun findContact(text: String, contacts: List<Person>): Person? {
        val lower = text.lowercase(Locale.ROOT)
        // Direct exact or substring match with contact name or telugu name
        for (contact in contacts) {
            val nameLower = contact.name.lowercase(Locale.ROOT)
            val teluguLower = contact.teluguName.lowercase(Locale.ROOT)

            if (nameLower.isNotBlank() && (lower.contains(nameLower) || isFuzzyMatch(lower, nameLower))) {
                return contact
            }
            if (teluguLower.isNotBlank() && (text.contains(contact.teluguName) || lower.contains(teluguLower))) {
                return contact
            }
        }
        return null
    }

    private fun isFuzzyMatch(text: String, name: String): Boolean {
        // e.g. "rameshki" contains "ramesh", "sulthan's" contains "sulthan"
        val cleanWords = text.split(" ", "కి", "కు", "తో", "నుండి", "దగ్గర", ",", ".", "'")
        return cleanWords.any { word ->
            word.trim().equals(name, ignoreCase = true) || word.startsWith(name, ignoreCase = true)
        }
    }

    fun extractCandidateName(text: String, contacts: List<Person>): String {
        // Remove known command words, numbers, and prepositions
        val stopWords = setOf(
            "i", "gave", "took", "from", "to", "me", "paid", "back", "received", "show", "how", "much",
            "does", "owe", "balance", "rupees", "rs", "money", "check", "the", "and", "a", "an",
            "తీసుకున్నాను", "ఇచ్చాను", "తిరిగి", "ఇచ్చాడు", "చెల్లించాడు", "బాకీ", "ఎంత", "దగ్గర",
            "నాకు", "కి", "కు", "నుండి", "నుంచి", "వచ్చింది", "కట్టాడు", "రూపాయలు", "వేలు", "వందలు"
        )

        // Split into tokens
        val tokens = text.replace(Regex("""[0-9₹,.\?!]"""), " ")
            .split(Regex("""\s+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (token in tokens) {
            val tokenClean = token.replace("కి", "").replace("కు", "").replace("తో", "").trim()
            if (tokenClean.length >= 2 && !stopWords.contains(tokenClean.lowercase(Locale.ROOT))) {
                return tokenClean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }

        return ""
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", amount)
        }
    }
}
