package com.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class DatabaseScaleDemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseScaleDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		runConcurrentUpdates();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;
	public void runConcurrentUpdates() throws InterruptedException {
		int threadCount = 50;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(1);

		for (int i = 0; i < threadCount; i++) {
			int id = (i % 1000) + 1;
			int finalI = i;
			executor.submit(() -> {
				try {
					System.out.println("Thread-" + finalI + " ready & waiting...");
					latch.await(); // all threads wait here
					System.out.println("Thread-" + finalI + " started DB update!");
					jdbcTemplate.update("UPDATE my_table SET count = count + 1 WHERE id = ?", id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		// Sleep for a moment just to ensure all threads are waiting
		Thread.sleep(2000);
		System.out.println("\n All threads ready... Releasing latch!\n");
		latch.countDown(); // trigger all threads at once

		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}

		System.out.println("\n All threads finished updating!");
	}

}
