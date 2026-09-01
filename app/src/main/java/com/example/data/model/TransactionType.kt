package com.example.data.model

enum class TransactionType {
    LENT,       // Shopkeeper gave money/credit to person (Person owes shopkeeper)
    BORROWED,   // Shopkeeper took money/credit from person (Shopkeeper owes person)
    REPAYMENT   // Settlement / Repayment (Received from customer or Paid to lender)
}

enum class TransactionDirection {
    GAVE,       // Money went OUT of shopkeeper's hand (Debit to other person)
    RECEIVED    // Money came IN to shopkeeper's hand (Credit/Repayment from other person)
}
