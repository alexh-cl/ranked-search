package com.ir

import scala.collection.mutable
import scala.io.Source

/** Author:       Alexander Hartmann,
  *               Holger Muth-Hellebrandt
  *
  * Task:         Assignment 3
  * Description:  Processes query of user input on inverted indices file.
  */

/**
  * Assignment 3.1
  * Class representing an inverted index with term frequencies
  */
class InvertedIndex {

  val inverted = mutable.HashMap[String, List[Array[Int]]]()

  def read(file: String): Unit = {
    val lines = Source.fromFile(file).getLines()

    for (line <- lines) {
      val term = line.split("\t")(0)
      val posting = line.split("\t")(1)
        .split("\\s+")
        .map(element => element.toInt)
        .sliding(2,2)
        .toList

      inverted += term -> posting
    }
  }

  def num_of_types: Int = {
    inverted.size
  }

  def get_postingList(term: String): List[Array[Int]] = {
    if(inverted.contains(term))
      inverted(term)
    else Nil
  }

}

/**
  * Assignment 3.2
  * Class for calculating IDF, cosine similarity & TF-IDF
  */
class QueryProcessor extends InvertedIndex {

  private val invertedIndex = inverted
  private var doc_number = 0


  def calculate_doc_num(): Unit = {
    val map_values = inverted.valuesIterator.toList.flatten
    val docs = mutable.HashSet[Int]()

    for (entry <- map_values) {
      docs.add(entry(0))
    }
    doc_number = docs.size
  }

  def get_idf(term: String): Float = {

    val N = doc_number
    val n_i = invertedIndex(term).size

    math.log(N/n_i).toFloat
  }

  def get_cos(query: List[String]): List[Float] = {

    var query_vector: List[Float] = Nil

    for(queryTerm <- query){
      if(invertedIndex.keySet.contains(queryTerm))
        query_vector :+= 1 * get_idf(queryTerm)
      else query_vector :+= 0.toFloat
    }


    println()
    print("query vector of ->V(q) ");println(query)
    println("->V(q) " + query_vector)
    println()

    //create doc vectors
    val doc_vectorList = Nil
    val doc_vector: List[Float] = Nil

    val doc2Score = mutable.HashMap[Int, List[Float]]()
    //fetching posting lists for t
    println(query.length)
    //    for(queryTerm <- query){
    for(index <- query.indices){
      val queryTerm = query(index)
      println(queryTerm + " occurrs in " + get_postingList(queryTerm).size + " documents.")

      for(pair <- get_postingList(queryTerm)){
        val doc_id = pair(0)
        val tf = pair(1)
        val tf_idf = tf * get_idf(queryTerm)

        if(!doc2Score.contains(doc_id)){
          var list: List[Float] = List.fill(query.length)(0)
          list = list.updated(index, tf_idf)

          doc2Score.put(doc_id, list)
        }else{
          doc2Score(doc_id) = doc2Score(doc_id).updated(index, tf_idf)
        }
      }
    }

    //calculate cosine similarity
    var results =  Array[(Int, Double)]()

    for((docid, scores) <- doc2Score){
      var dotproduct: Double = 0
      for(idx <- scores.indices){
        dotproduct += scores(idx) * query_vector(idx)
        //        println(query(idx))
        //        println("docid: " + docid + " doc_vector at "+ idx + ": " + scores(idx) + " query_vector at "
        //          + idx + ": "+ query_vector(idx) + " dotproduct: " + dotproduct)
      }

      var length_q: Double = 0
      for(idx <- query_vector.indices){
        length_q += query_vector(idx) * query_vector(idx)
//
//        println(query(idx))
//        println("docid: " + docid + " query_vector at "
//          + idx + ": "+ query_vector(idx) + " length_q: " + length_q)
      }
      length_q = Math.sqrt(length_q)


      var length_d: Double = 0
      for(idx <- scores.indices){
        length_d += scores(idx) * scores(idx)
      }
      length_d = Math.sqrt(length_d)

      val length = length_q * length_d

      val cosineSimilarity = dotproduct/length
//            println("doc_id : " + docid + " - cosineSimilarity: " + cosineSimilarity)


      results :+= (docid, cosineSimilarity)
    }
    results = results.sortWith(_._2 > _._2)
    println(results.deep.mkString(" "))

    doc_vectorList ::: doc_vector
    //cosine calculation
  }
}


object RankedSearch {

  /**
    * Main method
    */
  def main(args: Array[String]): Unit = {

    val r = new QueryProcessor

    //3.1
    r.read("reuters-21578-index-snowball.txt")
    println(s"Number of terms: ${r.num_of_types}")
    print(s"Posting list of 'hillard': ")
    print_array(r.get_postingList("hillard"))
    println()


    //3.2
    r.calculate_doc_num()
    println(s"IDF-Score of 'sugar': ${r.get_idf("sugar")}")
    println(r.get_cos("sugar soviet".split(" ").toList))
    println()

  }

  /**
    * Help function for correct usage
    */
  def help() = {
    println("Help function")
    sys.exit()
  }

  /**
    * print method for Option
    */
  def print_array(result: List[Array[Int]]) = {
    result.foreach(entry => print(entry.deep.mkString(" ") + " "))
    println
  }

}