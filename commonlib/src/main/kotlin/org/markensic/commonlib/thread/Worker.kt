package org.markensic.commonlib.thread

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.function.Consumer


class Worker(protected val executor: ExecutorService = Schedulers.io) {
  protected val futures = ConcurrentLinkedDeque<Future<*>>()

  fun execute(runnable: () -> Unit) {
    futures.add(executor.submit(runnable))
  }

  fun <T> add(runnable: () -> T) {
    futures.add(FutureTask(runnable))
  }

  fun <T> submit(callable: () -> T): Future<T> {
    return executor.submit(callable).also {
      futures.add(it)
    }
  }

  @Throws(Exception::class)
  fun await() {
    while (true) {
      futures.pollFirst()?.apply {
        if (this is FutureTask && !isDone) {
          run()
        }
      }?.get() ?: return
    }
  }

  fun <I> submitAndAwait(collection: Collection<I>, consumer: Consumer<I>) {
    collection.stream().map { f: I ->
      { consumer.accept(f) }
    }.forEach(::execute)
    await()
  }
}
