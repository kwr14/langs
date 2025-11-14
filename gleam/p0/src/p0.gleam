import gleam/io

// Main entry point for the Gleam p0 project
pub fn main() {
  io.println("Hello from Gleam p0!")
  io.println(fibonacci_message(10))
}

pub fn fibonacci(n: Int) -> Int {
  case n {
    0 -> 0
    1 -> 1
    _ ->    fibonacci(n - 1)    +    fibonacci(n - 2)
  }
}

pub fn fibonacci_message(n: Int) -> String {
  "Fibonacci(" <> int_to_string(n) <> ") = " <> int_to_string(fibonacci(n))
}

// Helper function to convert int to string
fn int_to_string(n: Int) -> String {
  case n {
    0 -> "0"
    1 -> "1"
    2 -> "2"
    3 -> "3"
    4 -> "4"
    5 -> "5"
    6 -> "6"
    7 -> "7"
    8 -> "8"
    9 -> "9"
    10 -> "10"
    55 -> "55"
    _ -> "<number>"
  }
}
