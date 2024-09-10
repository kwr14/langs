//! This example illustrates the way to send and receive statically typed JSON.
//!
//! In contrast to the arbitrary JSON example, this brings up the full power of
//! Rust compile-time type system guaranties though it requires a little bit
//! more code.

// These require the `serde` dependency.
use serde::{Deserialize, Serialize};
use std::arch::aarch64::int32x4_t;
use std::i32;
use std::{error::Error, fs::File, process};

#[derive(Debug, serde::Deserialize)]
struct Record {
    city: String,
    region: String,
    country: String,
    population: Option<u64>,
}

use std::fs::read_to_string;
use regex::Regex;

fn read_and_copy_matches(file_path: &str, pattern: &str) -> Result<Vec<String>, Box<dyn Error>> {
    let file_content = read_to_string(file_path)?;
    let re = Regex::new(pattern)?;
    let matches = re.find_iter(&file_content)
                    .map(|mat| mat.as_str().to_string())
                    .collect();
    Ok(matches)
}


fn example() -> Result<(), Box<dyn Error>> {
    let mut rdr = csv::Reader::from_reader(File::open("data.csv")?);
    for result in rdr.deserialize() {
        // Notice that we need to provide a type hint for automatic
        // deserialization.
        let record: Record = result?;
        println!("{:?}", record);
    }
    Ok(())
}                                                                                
#[derive(Debug, Serialize, Deserialize)]
struct Post {
    id: Option<i32>,
    title: String,
    body: String,
    #[serde(rename = "userId")]
    user_id: i32,
}


#[derive(Debug, Serialize, Deserialize)]
struct Product {
    id: i128,
    tags: String,
}

#[derive(Debug, Serialize, Deserialize)]
struct Products {
    products: Vec<Product>
}

// This is using the `tokio` runtime. You'll need the following dependency:
//
// `tokio = { version = "1", features = ["full"] }`
#[tokio::main]
async fn main() -> Result<(), reqwest::Error> {
    let new_post = Post {
        id: None,
        title: "Reqwest.rs".into(),
        body: "https://docs.rs/reqwest".into(),
        user_id: 1,
    };
    let post: Post = reqwest::Client::new()
        .post("https://jsonplaceholder.typicode.com/posts")
        .json(&new_post)
        .send()
        .await?
        .json()
        .await?;

    let products: Products =  reqwest::Client::new()
    .get("https://sheen-upholstery.myshopify.com/admin/api/2024-01/products.json?fields=id,tags")
    .header("X-Shopify-Access-Token", "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")
    .send()
    .await?
    .json()
    .await?;

    let skus = read_and_copy_matches("test", r"([A-Z])+\d+/\d+").unwrap();
    for sku in skus {
        let mut found = false;
        for product in &products.products {
            if product.tags.contains(&sku) {
                println!("SKU {} found in product tags.", sku);
                println!("{:?}", product);
                found = true;
                break;
            }
        }
        if !found {
            println!("SKU {} not found in any product tags.", sku);
        }
    }


    println!("{:#?}", post);

    if let Err(err) = example() {
        eprintln!("error running example: {}", err);
        process::exit(1);
    }

    Ok(())
}
