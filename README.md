# ICARUS

**ICARUS** stands for "Interactive platform for Corpus Analysis and Research tools, University of Stuttgart" and is a visualization and search tool geared towards dependency treebanks and/or corpora annotated for coreference. The original project page with additional information and associated publications can be found [here](https://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/icarus.en.html).

## Features

* Easy setup: only Java 7 or higher required to run ICARUS locally
* Desktop App, no server or network needed
* Highly customizable visualizations for
  * Dependency treebanks
  * Coreference annotations
  * Search results
  
* Flexible handling of corpus formats
  * Direct support for common CoNLL-style formats
  * Define your own tabular corpus format

* Powerful search functions
  * Example-based approach: generate queries directly from existing examples in a corpus or parser output 
  * Define queries graphically or textually
  * Simple bracket-style query language
  * Plug & Search: no pre-processing of corpora 
  * Wide range of search constraints: _token- or span-based annotations, regular expressions, type-specific operators (equals, less-than, unequal, contains-string, ...), hierarchical placement (root, leaf, ...), existential negation, disjunction, transitive closures for (syntactic) relations, ... _
  
* ICARUS for intonation
  * Visualize F<sub>0</sub> contours based on the PaIntE model
  * Use similarity measures for searching on PaIntE-annotated data
  * Play-back support on various levels of granularity (document, sentence, word, syllable) directly from within the ICARUS client to access the underlying audio

* Plugin framework to further extend functionality
  * Add support for new formats
  * Further customize visualizations
  * Introduce new search constraints

## Getting Started

To run ICARUS either double-click the 'icarus.jar' archive file (only recommended when working with small corpora) or run the ``java`` command with a customized heap size to give ICARUS more working memory (in this case 1 GB):
``java -Xms1g -Xmx1g -jar icarus.jar``

Note that ICARUS requires sufficient working memory to load all the desired corpora and/or parser models in case the integrated matetools parser is being used. While it is optimized for search performance (without using indexing), it is **not** geared towards huge corpora, such as web corpora!

## Contact

Please send questions and suggestions to [icarus@ims.uni-stuttgart.de](mailto:icarus@ims.uni-stuttgart.de).

## Documentation

A (slightly outdated) tutorial how to get started using ICARUS can be downloaded [here](https://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/icarus/Quickstart_Guide_ICARUS_1.05.pdf) (also included in the binary distribution).

The documentation which include some tutorials and [videos](http://wiki.ims.uni-stuttgart.de/extern/ICARUS-Search-Perspective#tutorials) can be found in our [wiki](http://wiki.ims.uni-stuttgart.de/extern/ICARUS). Note that this documentation is intended  for end users.

## Releases

The [releases](https://github.com/ICARUS-tooling/icarus1-platform/releases) section lists available ready-to-use binaries for download. Amongst them are also a couple legacy releases from older versions.