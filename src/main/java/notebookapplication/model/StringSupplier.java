package notebookapplication.model;

/**
 * A functional interface that supplies a string.
 * This can be used as a target for lambda expressions and method references
 *      that return a string.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * StringSupplier supplier = () -> "Hello, World!";
 * String result = supplier.get();
 * System.out.println(result);  // Prints "Hello, World!"
 * }</pre>
 */
@FunctionalInterface
public interface StringSupplier {
    /**
     * Gets a string result.
     *
     * @return  a string result
     */
    String get();
}
