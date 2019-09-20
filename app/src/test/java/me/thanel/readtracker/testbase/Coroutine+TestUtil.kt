package me.thanel.readtracker.testbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.mockito.Mockito.doAnswer
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
fun CoroutineScope.spyWithTestScope(testCoroutineScope: TestCoroutineScope) {
    doAnswer {
        val realCoroutineContext = it.callRealMethod() as CoroutineContext
        realCoroutineContext + testCoroutineScope.coroutineContext
    }.`when`(this).coroutineContext
}
