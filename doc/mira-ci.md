## MiRA

[MiRA: a Multi-aperture Image Reconstruction Algorithm](https://github.com/emmt/MiRA)

- `INIT_IMG` : initial image

- `MAXITER` : maximum number of iterations

- `RGL_WGT` : hyper-parameter tuning the strength of the regularization

- `RGL_NAME` **Regularization functions**

  - <u>Compactness</u>: (aka soft support) favors compact solutions where images structures are concentrated within a region of full-width at half maximum `RGL_GAMM` (in mas).

  - <u>hyperbolic</u> (aka Edge-preserving smoothness) favors smooth solutions while allowing sharp edges significantly higher than `RGL_TAU`. Using a very small `RGL_TAU` compared to the norm of the local gradients, mimics the effects of *total variation* (TV) regularizations.Conversely, using a very small edge threshold yields a regularization comparable to *quadratic smoothness*.

- **Bandwidth smearing** (chromatic aberration caused by the finite width of the spectral bandwidth)

  - `SMEAR_FN` : function modeling the effects of the spectral bandwidth smearing. Can be `none` if there is no bandwidth smearing, `sinc` for a cardinal sine or `gauss` for Gaussian (whose full-width at half-maximum matches that of the cardinal sine)

  - `SMEAR_FC` : strictly positive factor to scale the effects of the spectral bandwidth smearing. The full-width at half-maximum of the effective spectral bandwidth is assumed to be equal to `SMEAR_FC` times `EFF_BAND` parameter in the data. The default is `1.0`

### Reference
Thiébaut, É.: *"MiRA: an effective imaging algorithm for optical interferometry"* in SPIE Proc. Astronomical Telescopes and Instrumentation **7013**, 70131I-1-70131I-12 (2008) [doi](http://dx.doi.org/10.1117/12.788822).
