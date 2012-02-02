This package is a generic implementation of a method for producing spreadsheets out of pipeline graph. It is based on
the computation of the minimum flow from all the sources to all the sink nodes, having set at 1 the lower bound for all the experimental workflow arcs (and no upper bound, i.e. capacity) restrictions. A more detailed explanation of such algorithm will be added here soon. 

Start from graph2tab_intro.pdf for a detailed description on how to use the package. Have a look at MinFlowCalculator, FlowInitialiser, LayersBuilder, TableBuilder if you are interested in implementation details.

