package net.corda.examples.oracle.client

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.examples.oracle.base.contract.Prime
import net.corda.examples.oracle.client.flow.CreatePrime
import net.corda.examples.oracle.service.flow.QueryHandler
import net.corda.examples.oracle.service.flow.SignHandler
import net.corda.examples.oracle.service.service.Oracle
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PrimesClientTests {
    private val mockNet = MockNetwork()
    private lateinit var a: StartedNode<MockNode>

    @Before
    fun setUp() {
        setCordappPackages("net.corda.examples.oracle.service.service", "net.corda.examples.oracle.base.contract")

        val nodes = mockNet.createSomeNodes(1)
        a = nodes.partyNodes.first()

        val oracle = mockNet.createNode(nodes.mapNode.network.myAddress, legalName = CordaX500Name("Oracle", "New York","US"))
        listOf(QueryHandler::class.java, SignHandler::class.java).forEach { oracle.registerInitiatedFlow(it) }
        oracle.internals.installCordaService(Oracle::class.java)

        mockNet.runNetwork()
    }

    @After
    fun tearDown() {
        mockNet.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `oracle test`() {
        val flow = a.services.startFlow(CreatePrime(100))
        mockNet.runNetwork()
        val result = flow.resultFuture.getOrThrow().tx.outputsOfType<Prime.State>().single()
        assertEquals("The 100th prime number is 541.", result.toString())
    }

}
