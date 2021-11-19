## WISARD

[WISARD: Weak-phase Interferometric Sample Alternating Reconstruction Device](http://www.mariotti.fr/doc/approved/JMMC-MAN-2500-0001.pdf)

- `INIT_IMG` : initial image

- `MAXITER` : maximum number of iterations

- `RGL_WGT` : hyper-parameter tuning the strength of the regularization

- `RGL_NAME` **Regularization functions**

  - <u>soft support</u>:  favors compact solutions where images structures are concentrated on area with probability given by the map given by `RGL_PRIO`.

  - <u>L1L2White</u> (aka Edge-preserving smoothness) favors smooth solutions while allowing sharp edges significantly higher than `DELTA`. Using a very small `DELTA` compared to the norm of the local gradients, mimics the effects of *total variation* (TV) regularizations.

### Reference
L. M. Mugnier, G. Le Besnerais et S. Meimon: *"Inversion in Optical Imaging through Atmospheric Turbulence"*,
In J. Idier editor, Bayesian Approach to Inverse Problems,  Digital Signal and Image Processing
Series, chap. 10, pp. 243–283. ISTE / John Wiley, London (2008) [doi](https://doi.org/10.1002/9780470611197.ch10).