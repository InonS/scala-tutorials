package scala5marklewis.scala53ooadsus.scala534collections.scala5342maps

import java.io.File
import java.nio.file.Paths

import scala.io.{Source, StdIn}
import scala.util.{Failure, Success, Try}
import scala5marklewis.scala53ooadsus.scala534collections.scala5341sets.Scala53412BabyNameStateCoverage.{NameRecord, NameRecordCompanion}

/**
  * scala-project
  * Created by admin on 2016-11-27.
  */
object Scala53422BabyNamePopularYear extends App {

  def staticName() {
    val chosenName = "Elsie"

    def state() {

      object StateRecord extends YearCountRecordCompanion[StateRecord] {

        def apply(s: Array[String]): Option[StateRecord] = StateRecord(s(0), s(1)(0), s(2).toInt, s(3), s(4).toInt)

        def apply(state: String, gender: Char, year: Int, name: String, count: Int): Option[StateRecord] = {
          try {
            validate(state, gender, year, name, count)
            if (!fltr(state, gender, year, name, count)) return None // TODO replace by using groupedBy to allow dynamic queries by name
            Some(new StateRecord(state, gender == 'M', year, name, count))
          } catch {
            case t: Throwable => println((state, gender, year, name, count, t)); None
          }
        }

        private def validate(state: String, gender: Char, year: Int, name: String, count: Int) {
          require(state.length == 2)
          require(gender == 'F' || gender == 'M')
          require(year > 1900 && year < 2020)
          // require(name forall (c => c.isLetter && c.isTitleCase))
          require(count > 0)
        }
      }

      case class StateRecord(state: String /* length (2) */ ,
                             isMale: Boolean /* read 'M'/'F' */ ,
                             year: Int /* 1910 */ ,
                             name: String /* TitleCase */ ,
                             count: Int /* positive definite */) extends YearCountRecord

      val stateFiles = (Paths get("rsc", "BabyNames", "State")).toFile.listFiles() filter (fname => (fname.getName endsWith ".TXT") && fname.getName.length == 6)

      case class CountByYearForState(stateName: String, countByYear: Map[Int, Int])

      println("parsing:")
      val countByYearForStates: Array[CountByYearForState] = stateFiles.map(stateFile => CountByYearForState(stateFile.getName take 2, namesFromFile[StateRecord](stateFile, StateRecord, record => record.year -> record.count).get))

      println(countByYearForStates(0).countByYear)

      println("top year by state:")
      countByYearForStates.map(state_yearlyCounts => (state_yearlyCounts.stateName, state_yearlyCounts.countByYear.maxBy(kv => kv._2))).foreach(println)
    }

    def namesFromFile[R <: YearCountRecord](f: File, o: YearCountRecordCompanion[R], fieldsExtractor: R => (Int, Int)): Try[Map[Int, Int]] = {
      println(s"parsing ${f.getName}")
      val source = Source fromFile f
      try {
        val lineIterator = source.getLines filter (_ nonEmpty)
        val csv = lineIterator.map(_ split ",")
        val records = csv.map(o(_)).filter(_.nonEmpty).map(_ get)
        val countByYear = records.map(fieldsExtractor).toMap
        Success(countByYear)
      } catch {
        case t: Throwable => Failure(t)
      } finally {
        source.close()
      }
    }

    def fltr(state: String, gender: Char, year: Int, name: String, count: Int): Boolean = {
      chosenName equalsIgnoreCase name
    }

    state()
  }

  staticName()

  def dynamicName() {

    def state() {

      object StateRecord extends YearCountRecordCompanion[StateRecord] with NameRecordCompanion[StateRecord] {

        def apply(s: Array[String]): Option[StateRecord] = StateRecord(s(0), s(1)(0), s(2).toInt, s(3), s(4).toInt)

        def apply(state: String, gender: Char, year: Int, name: String, count: Int): Option[StateRecord] = {
          try {
            validate(state, gender, year, name, count)
            Some(new StateRecord(state, gender == 'M', year, name, count))
          } catch {
            case t: Throwable => println((state, gender, year, name, count, t)); None
          }
        }

        private def validate(state: String, gender: Char, year: Int, name: String, count: Int) {
          require(state.length == 2)
          require(gender == 'F' || gender == 'M')
          require(year > 1900 && year < 2020)
          // require(name forall (c => c.isLetter && c.isTitleCase))
          require(count > 0)
        }
      }

      case class StateRecord(state: String /* length (2) */ ,
                             isMale: Boolean /* read 'M'/'F' */ ,
                             year: Int /* 1910 */ ,
                             name: String /* TitleCase */ ,
                             count: Int /* positive definite */) extends YearCountRecord with NameRecord

      val stateFiles = (Paths get("rsc", "BabyNames", "State")).toFile.listFiles() filter (fname => (fname.getName endsWith ".TXT") && fname.getName.length == 6)

      case class RecordsByStateThenName(stateName: String, stateRecordsByName: Map[String, Set[StateRecord]])

      println("parsing:")
      val records = stateFiles.map(stateFile => RecordsByStateThenName(stateFile.getName take 2, namesFromFile[StateRecord](stateFile, StateRecord, null).get))

      println(records(0).stateRecordsByName)

      def topStateRecordsByChosenName() {
        var chosenName = ""
        while (chosenName != "quit") {
          chosenName = StdIn readLine "select name:"
          println("top year by state:")
          try {
            val maybeStateRecordses = records.map(_ stateRecordsByName).map(_ get chosenName)
            val topStateRecords = maybeStateRecordses.filterNot(_ isEmpty).map(_ get).map(s => s.maxBy(_ count)).map(r => s"${r.state}, ${r.year}, ${r.count}")
            println(topStateRecords.mkString("\n"))
          } catch {
            case t: Throwable => println(t)
          }
        }
      }

      topStateRecordsByChosenName()
    }

    def namesFromFile[R <: YearCountRecord with NameRecord](f: File, o: YearCountRecordCompanion[R], fieldsExtractor: R => (Int, Int)): Try[Map[String, Set[R]]] = {
      println(s"parsing ${f.getName}")
      val source = Source fromFile f
      try {
        val lineIterator = source.getLines filter (_ nonEmpty)
        val csv = lineIterator.map(_ split ",")
        val recordsByName: Map[String, Set[R]] = csv.map(o(_)).filter(_.nonEmpty).map(_ get).toSet.groupBy(_ name)
        Success(recordsByName)
      } catch {
        case t: Throwable => Failure(t)
      } finally {
        source.close()
      }
    }

    state()
  }

  trait YearCountRecord {
    def year: Int

    def count: Int
  }

  trait YearCountRecordCompanion[R <: YearCountRecord] {
    def apply(s: Array[String]): Option[R]
  }

  dynamicName()
}

