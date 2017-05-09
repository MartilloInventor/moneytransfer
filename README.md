The basic data model consists of the following classes:

1. Account,which has an account id and which can either be a source or
a destination;

2. Transfer, which specifies a source Account, a destination Account,
and an Amount;

3. Amount

These classes can reference description classes.

Amount can have a numeric value, which should probably be BigDecimal,
and should reference a Currency, which itself also references a
description class.

The final necessary class structure could be a good deal more complex
than the problem envisions.

For example a User, who probably has a Loginname and Password,
probably has multiple Accounts for multiple Purposes and probably each
denominated in at least one currency. To tell the truth there is no
reason to restrict Accounts to Currencies. They might be denominated
in Commodities like gold, silver, copper, oil, etc.

Here are URLs for the BigDecimal and Currency Java classes.

https://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html

https://docs.oracle.com/javase/7/docs/api/java/util/Currency.html

(The Currency class would probably have to be extended in a complete solution.)

Unfortunately there seems to be no standard Java class for financial instruments.

The endpoints of the rest API (relative to /api) are:

/ -- ping

/accounts GET -- returns all accounts in a JSON list

/accounts/{ID} GET -- returns JSON descriptions of account

/transfer PUSH srcid=STRING dstid=STRING amount=FLOAT instrument=STRING


