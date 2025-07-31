package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Example
import ponder.galaxy.model.data.NewExample
import pondui.io.ApiClient
import pondui.io.globalApiClient

class ExampleStore(val client: ApiClient = globalApiClient) {
    suspend fun readExample(exampleId: Long) = client.get(Api.Examples, exampleId)
    suspend fun readUserExamples() = client.get(Api.Examples.User)
    suspend fun createExample(newExample: NewExample) = client.post(Api.Examples.Create, newExample)
    suspend fun updateExample(example: Example) = client.update(Api.Examples.Update, example)
    suspend fun deleteExample(exampleId: Long) = client.delete(Api.Examples.Delete, exampleId)
}