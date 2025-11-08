import unittest

from main import fibonacci


class TestFibonacci(unittest.TestCase):
    def test_base_cases(self):
        self.assertEqual(fibonacci(0), 0)
        self.assertEqual(fibonacci(1), 1)

    def test_small_values(self):
        self.assertEqual(fibonacci(2), 1)
        self.assertEqual(fibonacci(3), 2)
        self.assertEqual(fibonacci(4), 3)
        self.assertEqual(fibonacci(5), 5)

    def test_larger_value(self):
        # Known value
        self.assertEqual(fibonacci(10), 55)
        self.assertEqual(fibonacci(20), 6765)

    def test_negative_raises(self):
        with self.assertRaises(ValueError):
            fibonacci(-1)


if __name__ == "__main__":
    unittest.main()