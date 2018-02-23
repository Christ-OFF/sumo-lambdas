# Problem
The lamba was working this way : 
1. RikishisScrap scrap the list of Rikishis (>600)
2. Pushes to AWS SNS on event for each riskishi
3. Each rikishiscrap is called and scrap its rikishi  

But the website does not support so many requests as SNS can only limit its rate as 1 per second
the website faces 10 request or more as a task may take 40s

# Solution
1. RikishisScrap scrap the list of Rikishis (>600)
2. Pushes to AWS SNS on event with the full list of RikishisIds
3. The RikishiScrap is called take the first id in the list
4. It extracts Rikishi info or fail
5. Send new list with the first one removed
6. a new rikishi is scrapped...

If the Lambda crashes or timeout the "chain" is broken !
So the http timeout must be below 60sec (the lambda limit)
