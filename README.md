# Starling tech test

This is an implementation of a round-up function. 
The program takes an API token and a savings goal UID, 
then rounds up all the transactions for the last week and transfers them into the savings goal.

## Rationales

The reason I chose to take the savings goal UID, is that I imagine this is similar to how an actual back-end
would process this task.

This application does _not_ take different currencies into account, as this would complicate the task further
(and require some type of currency conversion which I consider to be out of scope).

From a security perspective, I concede that passing the token on the command line is less than ideal. I am assuming 
these tokens are very short-lived and they only apply to sandbox accounts, so the risk is somewhat less.
In a production system I would, for a client-side application, allow the user to authenticate themselves.

I wrote my own deserialiser for the `Transaction` object as this would otherwise create a fairly difficult-to-work-with
object hierarchy. In production code I would be perhaps slightly more wary of this; for this task, I wanted to elegantly
express the domain logic in POJOs.

I considered splitting out the `ApiClient` but the amount of reused state led me to decide against this. This class is
definitely at the limit, and upon any additional API calls would have to lead to a refactor.

I used the `Transaction` and the `Account` objects as DTOs as well as the core domain logic; generally this is not such
a great idea because it couples your internal domain to an external data definition. For this example I have considered
the APIs that are called to be stable, and avoided over-complicating my solution.