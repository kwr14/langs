import unittest

from hypothesis import given, strategies as st

from main import fibonacci


class TestFibonacciProperty(unittest.TestCase):
    @given(st.integers(min_value=2, max_value=1000))
    def test_recurrence_relation(self, n: int):
        # For n >= 2, F(n) = F(n-1) + F(n-2)
        self.assertEqual(fibonacci(n), fibonacci(n - 1) + fibonacci(n - 2))

    @given(st.integers(min_value=1, max_value=1000))
    def test_monotone_non_decreasing(self, n: int):
        # Sequence is non-decreasing from n >= 1
        self.assertGreaterEqual(fibonacci(n), fibonacci(n - 1))

    @given(st.integers(min_value=0, max_value=1000))
    def test_non_negative(self, n: int):
        # All Fibonacci numbers for n >= 0 are non-negative
        self.assertGreaterEqual(fibonacci(n), 0)


if __name__ == "__main__":
    unittest.main()