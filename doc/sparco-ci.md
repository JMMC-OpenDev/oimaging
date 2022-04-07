## SPARCO

[SPARCO (Semi-Parametric Approach for Reconstruction of Chromatic Objects) mira  plugin](https://github.com/kluskaj/mira-sparco-multi)

SPARCO is an approach where polychromatic optical interferometric data is modeled as a sum of  a geometrical model and a reconstructed image with different spectra. In this framework, the only the image is reconstructed using the MiRA algorithm (with its parameters) whereas the model is described using the the SPARCO parameters.

### Parameters in common with MiRA

- `INIT_IMG` : initial image
- `MAXITER` : maximum number of iterations
- `RGL_WGT` : hyper-parameter tuning the strength of the regularization
- `RGL_NAME` **Regularization functions**
  - <u>Compactness</u>: (aka soft support) favors compact solutions where images structures are concentrated within a region of full-width at half maximum `RGL_GAMM` (in mas).
  - <u>hyperbolic</u> (aka Edge-preserving smoothness) favors smooth solutions while allowing sharp edges significantly higher than `RGL_TAU`. Using a very small `RGL_TAU` compared to the norm of the local gradients, mimics the effects of *total variation* (TV) regularizations.Conversely, using a very small edge threshold yields a regularization comparable to *quadratic smoothness*.
- **Bandwidth smearing** (chromatic aberration caused by the finite width of the spectral bandwidth)
  - `SMEAR_FN` : function modeling the effects of the spectral bandwidth smearing. Can be `none` if there is no bandwidth smearing, `sinc` for a cardinal sine or `gauss` for Gaussian (whose full-width at half-maximum matches that of the cardinal sine)
  - `SMEAR_FC` : strictly positive factor to scale the effects of the spectral bandwidth smearing. The full-width at half-maximum of the effective spectral bandwidth is assumed to be equal to `SMEAR_FC` times `EFF_BAND` parameter in the data. The default is `1.0`

### SPARCO specific parameters:

- `SWAVE0` : reference wavelength where flux ratios are enforced

- `SPEC0`:  type of spectrum for the reconstructed image. Can be either:
  
  - `pow` for a power law with spectral index `SPEC0`
  
  - `BB` for a blackbody with temperature `STEM0`

- `SNMODS` : number of geometrical models

For each geometrical model `n`we have:

* `SMODn`: type of geometric model. It can be either
  
  * `star`: a point source
  
  * `UD`: a uniform disk of radius`SPARn`
  
  * `bg`: an over-resolved background

* `SFLUn`: flux ratio a the model `n`at wavelength  `SWAVE0`

* `SPECn`: spectrum for the geometric model `n`.  It can be either:
  
  - `pow` for a power law with spectral index `SPECn`
  
  - `BB` for a blackbody with temperature `STEMn`

* `SDEXn`: the RA shift of the model `n` relatively to the center of the image

* `SDEYn`: the Dec shift of the model `n` relatively to the center of the image

### Reference

Kluska, J. et al.: *"SPARCO : a semi-parametric approach for image reconstruction of chromatic objects. Application to young stellar objects"* in Astronomy & Astrophysics, Volume 564, id.A80, 11 pp. [DOI](https://ui.adsabs.harvard.edu/link_gateway/2014A&A...564A..80K/doi:10.1051/0004-6361/201322926)




