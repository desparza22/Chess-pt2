
library(tidyverse)

gameEvaluations <- read.csv("CS_Ventures/Chess2/finding-elo/gameEvaluations.txt", header=F)
gameEval2 <- read.csv("CS_Ventures/Chess2/finding-elo/gameEvaluations2.txt", header=F)

hist(log(abs(gameEvaluations$V1), 20))

hist(abs(gameEvaluations$V1)^.5)

eval <- as_tibble(gameEvaluations)
eval2 <- as_tibble(gameEval2)

eval <- eval %>% mutate(sign = ifelse(V1 >= 0, 1, -1))
eval <- eval %>% mutate(adjusted = 0.5 + .57 * log( log(abs(V1) + e, e) , 10)^5 * sign)

e <- exp(1)

hist(abs(eval$V1)^.25 * eval$sign)
hist(eval$V1)
hist(log(log(abs(eval$V1)+e, e), 10) * eval$sign)
hist(log(abs(eval$V1), 2.718) * eval$sign)
hist(eval$V1)

max <- max(eval$V1)
min <- min(eval$V1)

adjMax <- log( log(abs(max) + e, e) , 10)^5
adjMin <- log( log(abs(min) + e, e) , 10)^5 * -1

.5/adjMax
1/adjMin

hist(0.5 + .57 * log( log(abs(eval$V1) + e, e) , 10)^5 * eval$sign)
hist(eval2$V1)
V3 <- eval$adjusted - eval2$V1
hist(V3)

eval2 <- eval2 %>% mutate(guessSuccess = abs(0.5 - V1))
hist(eval2$guessSuccess)
mean(eval2$guessSuccess)
