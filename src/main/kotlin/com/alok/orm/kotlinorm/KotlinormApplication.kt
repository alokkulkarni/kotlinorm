package com.alok.orm.kotlinorm

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@ImportRuntimeHints(ExposedHints::class)
@SpringBootApplication
class KotlinormApplication

fun main(args: Array<String>) {
	runApplication<KotlinormApplication>(*args)
}


@Component
@Transactional
class CustomerDemo: ApplicationRunner {

	override fun run(args: ApplicationArguments?) {
		SchemaUtils.create(CitiesTable, CustomersTable, OrdersTable)

		OrdersTable.deleteAll()
		CustomersTable.deleteAll()
		CitiesTable.deleteAll()

		val cityList = listOf("St. Petersburg", "Munich", "Prague")
			.map { City(null, it) }
			.map { city ->
				CitiesTable.insertAndGetId {
					it[name] = city.name
				}
			}
			.map { it.value }    // get the id of the city

		val customerList = listOf("Alice", "Bob", "Carol")
			.mapIndexed { index, name -> Customer(null, name, 21 + index) }
			.map { customer ->
				CustomersTable.insertAndGetId {
					it[name] = customer.name
					it[age] = customer.age
					it[city] = cityList.random()
				}
			}
			.map { it.value }    // get the id of the customer

		val orderList = listOf("SKU1", "SKU2", "SKU3")
			.map { Order(0, it) }
			.map { order ->
				OrdersTable.insertAndGetId {
					it[sku] = order.sku
					it[orderDate] = java.time.LocalDate.now()
					it[customer] = customerList.random()
				}
			}
			.map { it.value }    // get the id of the order
	}
}


//DTO Objects
// Path: src/main/kotlin/com/alok/orm/kotlinorm/DTO.kt

data class Customer(val id: Int?, val name: String, val age: Int)
data class City(val id: Int?, val name: String)
data class Order(val id: Int, val sku: String)


//Object Relational Mapping (ORM) is a programming technique for converting data between incompatible type systems
// using object-oriented programming languages. Exposed is a lightweight SQL library written in Kotlin for
// Kotlin ORM Exposed is a lightweight SQL library written in Kotlin for

 object CustomersTable : IntIdTable("customer") {
	 val name = varchar("name", 50) // Column<String>
	 val age = integer("age") // Column<Int>
	 val city = reference("cityId", CitiesTable) // Column<String>
 }

 object CitiesTable : IntIdTable("city") {
	 val name = varchar("name", 50) // Column<String>
 }

 object OrdersTable : IntIdTable("order") {
	 val sku = text("sku") // Column<String>
	 val orderDate = date("orderDate") // Column<DateTime>
	 val customer = reference("customerId", CustomersTable) // Column<Int>
 }


//this is to expose the Exposed classes to the reflection hints so that they can be used in the AOT compilation
// process
// Path: src/main/kotlin/com/alok/orm/kotlinorm/ExposedHints.kt

class ExposedHints : RuntimeHintsRegistrar {

	override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

		arrayOf(
			org.jetbrains.exposed.spring.DatabaseInitializer::class,
			org.jetbrains.exposed.spring.SpringTransactionManager::class,
			java.util.Collections::class,
			Column::class,
			Database::class,
			Op::class,
			Op.Companion::class,
			DdlAware::class,
			Expression::class,
			ExpressionWithColumnType::class,
			ColumnType::class,
			DatabaseConfig::class,
			IColumnType::class,
			IntegerColumnType::class,
			PreparedStatementApi::class,
			ForeignKeyConstraint::class,
			IColumnType::class,
			QueryBuilder::class,
			Table::class,
			Transaction::class,
			TransactionManager::class,
			Column::class,
			Database::class,
			kotlin.jvm.functions.Function0::class,
			kotlin.jvm.functions.Function1::class,
			kotlin.jvm.functions.Function2::class,
			kotlin.jvm.functions.Function3::class,
			kotlin.jvm.functions.Function4::class,
			kotlin.jvm.functions.Function5::class,
			kotlin.jvm.functions.Function6::class,
			kotlin.jvm.functions.Function7::class,
			kotlin.jvm.functions.Function8::class,
			kotlin.jvm.functions.Function9::class,
			kotlin.jvm.functions.Function10::class,
			kotlin.jvm.functions.Function11::class,
			kotlin.jvm.functions.Function12::class,
			kotlin.jvm.functions.Function13::class,
			kotlin.jvm.functions.Function14::class,
			kotlin.jvm.functions.Function15::class,
			kotlin.jvm.functions.Function16::class,
			kotlin.jvm.functions.Function17::class,
			kotlin.jvm.functions.Function18::class,
			kotlin.jvm.functions.Function19::class,
			kotlin.jvm.functions.Function20::class,
			kotlin.jvm.functions.Function21::class,
			kotlin.jvm.functions.Function22::class,
			kotlin.jvm.functions.FunctionN::class
		)
			.map { it.java }
			.forEach {
				hints.reflection().registerType(it, *MemberCategory.entries.toTypedArray())
			}

		arrayOf(
			"META-INF/services/org.jetbrains.exposed.dao.id.EntityIDFactory",
			"META-INF/services/org.jetbrains.exposed.sql.DatabaseConnectionAutoRegistration",
			"META-INF/services/org.jetbrains.exposed.sql.statements.GlobalStatementInterceptor"
		)
			.map { ClassPathResource(it) }
			.forEach { hints.resources().registerResource(it) }
	}
}