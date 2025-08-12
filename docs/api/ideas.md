# Ideas API

- GET Api.Ideas.ByStar ("/api/v1/idea/by_star/{id}")
  - Path param: id = StarId string
  - Response: Idea (created on-demand if missing)

- GET Api.Ideas.Stars ("/api/v1/idea/star/{id}")
  - Path param: id = StarId string
  - Response: List<Idea>
