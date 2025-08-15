package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Example
import ponder.galaxy.model.data.ExampleId
import ponder.galaxy.model.data.NewExample
import pondui.io.NeoApiClient
import pondui.io.globalApiClient
import pondui.io.globalNeoApiClient

class ExampleStore(val client: NeoApiClient = globalNeoApiClient) {
    suspend fun readExample(exampleId: ExampleId) = client.getById(Api.Examples, exampleId)
    suspend fun readUserExamples() = client.request(Api.Examples.User)
    suspend fun createExample(newExample: NewExample) = client.request(Api.Examples.Create, newExample)
    suspend fun updateExample(example: Example) = client.request(Api.Examples.Update, example)
    suspend fun deleteExample(exampleId: Long) = client.request(Api.Examples.Delete, exampleId)
}