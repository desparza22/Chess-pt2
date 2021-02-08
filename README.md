# chess-pt2
To do: 
Currently, the neural network relies on two nodes, A and B, to indicate whether it is white's turn (A is activated and B is deactivated) or black's (the reverse). Of course, the same information could be communicated with a single node. Both of these options, however, place too much pressure on the network to recognize the role of these nodes, greatly slowing down the process or even making it inachievable. 
Instead, I am changing the implementation so that the network will always evaluate a position from the perspective of the color whose turn it is to play. After a position is evaluated, the evaluation will be multiplied by -1 if it is black's turn to play. 

A second attempt at a chess computer, evaluating boards with a neural network and calculating moves with a bit board
