import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class ParallelMonteCarloPi {
    private static final long TOTAL_ITERATIONS = 1_000_000_000L;

    public static void main(String[] args) {
        int threadsCount = 4;
        if (args.length > 0) {
            try {
                threadsCount = Integer.parseInt(args[0]);
                if (threadsCount <= 0) {
                    System.out.println("Кількість потоків має бути більшою за 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Помилка: аргумент має бути цілим числом.");
                return;
            }
        }

        long startTime = System.nanoTime();

        Thread[] threads = new Thread[threadsCount];
        long[] results = new long[threadsCount];
        long baseIterations = TOTAL_ITERATIONS / threadsCount;
        long remainder = TOTAL_ITERATIONS % threadsCount;

        for (int i = 0; i < threadsCount; i++) {
            final int threadIndex = i;
            final long iterationsForThread = baseIterations + (i == threadsCount - 1 ? remainder : 0);

            threads[i] = new Thread(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                long insideCircle = 0;

                for (long j = 0; j < iterationsForThread; j++) {
                    double x = random.nextDouble();
                    double y = random.nextDouble();
                    if (x * x + y * y <= 1.0) {
                        insideCircle++;
                    }
                }
                results[threadIndex] = insideCircle;
            });
            threads[i].start();
        }

        long totalInsideCircle = 0;
        for (int i = 0; i < threadsCount; i++) {
            try {
                threads[i].join();
                totalInsideCircle += results[i];
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Потік було перервано.");
                return;
            }
        }

        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        double pi = 4.0 * totalInsideCircle / TOTAL_ITERATIONS;

        System.out.printf(Locale.US, "PI is %.5f%n", pi);
        System.out.println("THREADS " + threadsCount);
        System.out.printf(Locale.US, "ITERATIONS %,d%n", TOTAL_ITERATIONS);
        System.out.printf(Locale.US, "TIME %.2fms%n", timeMs);
    }
}