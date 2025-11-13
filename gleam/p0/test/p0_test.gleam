import gleeunit
import gleeunit/should
import p0

pub fn main() {
  gleeunit.main()
}

pub fn fibonacci_base_cases_test() {
  p0.fibonacci(0)
  |> should.equal(0)
  
  p0.fibonacci(1)
  |> should.equal(1)
}

pub fn fibonacci_recursive_test() {
  p0.fibonacci(2)
  |> should.equal(1)
  
  p0.fibonacci(3)
  |> should.equal(2)
  
  p0.fibonacci(4)
  |> should.equal(3)
  
  p0.fibonacci(5)
  |> should.equal(5)
  
  p0.fibonacci(10)
  |> should.equal(55)
}

