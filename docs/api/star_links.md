# Star Links API

- GET Api.StarLinks.Outgoing ("/api/v1/star_link/links/{starId}")
  - Path param: starId = StarId string
  - Response: List<StarLink>

Client helper:
- StarLinkApiClient.readOutgoingLinks(starId: StarId): List<StarLink>?
