# chess-pt2

Current work:
The neural network is being built up from the very base level of information necessary to represent a board. This is done by adding redundancies to the information captured by the nodes to improve the network's ability to "see".
Examples: 
1. There are now 64 nodes representing whether a square is an empty space or occupied. While this could be deduced by the presence / lack of presence of all other pieces, now relationships can more easily be formed between a piece and the presence or absence of an empty space near it. 
2. Instead of using a node to represent whether it is white or black's turn, the nodes are flipped if it is blacks turn, so that the position is always evaluated for whoever's turn it is to play. When producing a numerical evaluation, the value is flipped around 0.5 if it is blacks turn to play, where 0.5 represents a tied position on a scale of 0 (black win) to 1 (white win).

A second attempt at a chess computer, evaluating boards with a neural network and calculating moves with a bit board
