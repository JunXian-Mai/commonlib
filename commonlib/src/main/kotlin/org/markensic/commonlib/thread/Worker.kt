package org.markensic.commonlib.thread

import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.function.Consumer


class Worker(protected val executor: ExecutorService = Schedulers.io) {
  protected val futures = ConcurrentLinkedDeque<Future<*>>()

  fun execute(runnable: () -> Unit) {
    futures.add(executor.submit(runnable))
  }

  fun <T> submit(callable: Callable<T>): Future<T> {
    return executor.submit<T>(callable).also {
      futures.add(it)
    }
  }

  @Throws(Exception::class)
  fun await() {
    while (true) {
      futures.pollFirst()?.get() ?: return
    }
  }

  fun <I> submitAndAwait(collection: Collection<I>, consumer: Consumer<I>) {
    collection.stream().map { f: I ->
      { consumer.accept(f) }
    }.forEach(::execute)
    await()
  }
}
