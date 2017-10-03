# graph2tab, a library to convert experimental workflow graphs into tabular formats

This package is a generic implementation of a method for producing spreadsheets out of experimental graphs. It is based on the computation of the minimum flow from all the sources to all the sink nodes, having set at 1 the lower bound for all the experimental workflow arcs (and no upper bound, i.e. no capacity) restrictions.

Start from [this guide](http://github.com/ISA-tools/graph2tab/blob/master/graph2tab_intro.pdf) for a detailed description on how to use the package. Have a look at `MinFlowCalculator`, `FlowInitialiser`, `LayersBuilder`, `TableBuilder` if you are interested in implementation details.

**Links**

* [Presentation at the EBI](https://www.slideshare.net/mbrandizi/graph2tab-introduction)
* [Application Note on Bioinformatics](http://bioinformatics.oxfordjournals.org/content/28/12/1665)
* [Theoretical analysis on correctness and performance](https://oup.silverchair-cdn.com/oup/backfile/Content_public/Journal/bioinformatics/28/12/10.1093_bioinformatics_bts258/1/bts258_Supplementary_Data.zip?Expires=1507161884&Signature=b1mhaKvq45gvlDrufIk1vBszB7xsT15oIDH4GOXVSohtpSkQD5jLSidXKnh0HLK8sNl6uZehAELwXzzhaA8SUTHSExlFm1QkuH~IpqQKKMyLZOYzRyV~nK7MnD1NW0k6FiY4rOv7AF9fLedpbWLBXxmWEV8Xu-GVi~Ovxt6rmFPMMDX5sjVyJwGLDNHAaKeSiUaus~ZOXza8nmFuJly3pbDdCwGc90q3SmNB0GpsHSMcJ0spLFtqFkMNqkJRX9308oEIYfWhb~5XMalQA8yOUTizYDCAxpFJRrZLYGUabX5IHvU5LdpOQHrXd-sTa0NljlLaCiZpHUpC72zetbo91Q__&Key-Pair-Id=APKAIUCZBIA4LVPAVW3Q) (from the same paper).
