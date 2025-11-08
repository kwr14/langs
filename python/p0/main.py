def fibonacci(n: int) -> int:
    """Compute the n-th Fibonacci number (0-indexed).

    Uses an iterative approach for efficiency. Raises ValueError for negatives.
    """
    if n < 0:
        raise ValueError("n must be non-negative")
    if n == 0:
        return 0
    if n == 1:
        return 1
    a, b = 0, 1
    for _ in range(2, n + 1):
        a, b = b, a + b
    return b


def main():
    import sys

    # Default n=10 if not provided
    try:
        n = int(sys.argv[1]) if len(sys.argv) > 1 else 10
    except ValueError:
        print("Usage: python main.py [n:int]")
        sys.exit(2)

    print(f"fib({n}) = {fibonacci(n)}")


if __name__ == "__main__":
    main()
