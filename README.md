# graph2tab, a library to convert experimental workflow graphs into tabular formats

This package is a generic implementation of a method for producing spreadsheets out of experimental graphs. It is based on the computation of the minimum flow from all the sources to all the sink nodes, having set at 1 the lower bound for all the experimental workflow arcs (and no upper bound, i.e. no capacity) restrictions.

Start from [this guide](http://github.com/ISA-tools/graph2tab/blob/master/graph2tab_intro.pdf) for a detailed description on how to use the package. Have a look at `MinFlowCalculator`, `FlowInitialiser`, `LayersBuilder`, `TableBuilder` if you are interested in implementation details.

**Links**

* [Presentation at the EBI](https://www.slideshare.net/mbrandizi/graph2tab-introduction)
* [Application Note on Bioinformatics](http://bioinformatics.oxfordjournals.org/content/28/12/1665)
* [Theoretical analysis on correctness and performance](http://bioinformatics.oxfordjournals.org/content/suppl/2012/04/29/bts258.DC1/supplemental_material_v1_1.pdf) (from the same paper).
