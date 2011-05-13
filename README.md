This package is a generic implementation of a method for producing spreadsheets out of pipeline graph. It is based on
the node splitting metod: all the nodes in the input pipeline are reduced to *normalized* nodes, i.e.: nodes that have
at most one input and at most one output. This is done by taking those nodes having splitting or pooling, creating
copies of them and distributing the excessive inputs or outputs over the copies.

Start from graph2tab_intro.pdf for a detailed description on how to use the package. Have a look at ChainsBuilder, 
LayersBuilder, TableBuilder, if you are interested in implementation details.
