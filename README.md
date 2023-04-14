# VNetFetch
VNetFetch is a custom utility designed to efficiently download files using HTTP, while implementing caching with E-Tags and SHA1 for validation. This library aims to streamline the process of fetching files from the web, such as JSON files or other similar formats, for multiple projects.

## Motivation
The need for VNetFetch arose from multiple ongoing projects that required fetching files from the web. By creating this library and implementing caching, the overall speed and efficiency of the file retrieval process can be significantly improved.

## Alternatives
While the Apache HTTP Client used as the backend for VNetFetch has built-in caching, it proved to be too clunky to achieve the desired behavior. Other enterprise solutions were considered, but they either required a license or were equally cumbersome to set up and use.

## Implementation
The VNetFetch implementation prioritizes fetching files from disk storage, provided that both the SHA1 hash and E-Tag are valid. If either condition is not met, the cache for the specified file is cleared, and the file is cached again.

The Apache HTTP Client was chosen as the preferred option, as it offers a more straightforward approach compared to the built-in Java implementation.

### Flow Diagram
![Flow Diagram of the VNetFetch Implementation](vnetfetch_implementation.svg)

## Future Improvements
- Enhance the API by adopting a Provider Pattern
- Refactor the internals and add JavaDoc where relevant for better code readability and maintainability
