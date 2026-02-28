package com.ghostid.app.domain.generator

import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.AliasAddress
import com.ghostid.app.domain.model.AliasName
import com.ghostid.app.domain.model.aliasAccentColors
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AliasGenerator @Inject constructor(
    private val accountSuggester: AccountSuggester,
) {
    private val rng = SecureRandom()

    private val firstNames = listOf(
        "Alex", "Jordan", "Morgan", "Taylor", "Casey", "Riley", "Avery", "Quinn",
        "Skyler", "Peyton", "Dakota", "Sage", "River", "Reese", "Finley", "Blake",
        "Drew", "Hayden", "Cameron", "Jamie", "Logan", "Emery", "Remi", "Rowan",
        "Charlie", "Addison", "Elliot", "Harper", "Kai", "Lee", "Marlowe", "Noel",
        "Parker", "Phoenix", "Presley", "Sasha", "Shea", "Spencer", "Stevie", "Storm",
    )

    private val lastNames = listOf(
        "Anderson", "Brooks", "Campbell", "Davies", "Evans", "Foster", "Gray",
        "Harris", "Johnson", "King", "Lewis", "Mitchell", "Nelson", "O'Brien",
        "Parker", "Quinn", "Roberts", "Smith", "Taylor", "Turner", "Walker",
        "White", "Wilson", "Young", "Clarke", "Cooper", "Edwards", "Hall",
        "Hughes", "Jackson", "Kelly", "Martin", "Moore", "Price", "Reed",
        "Scott", "Shaw", "Stone", "Thompson", "Webb", "Wood", "Wright",
    )

    private val nationalities = listOf(
        "American", "British", "Canadian", "Australian", "German", "French",
        "Dutch", "Swedish", "Norwegian", "Danish", "Finnish", "Swiss",
        "Austrian", "Belgian", "Irish", "New Zealander", "South African",
        "Singaporean", "Japanese", "South Korean",
    )

    private val occupations = listOf(
        "Software Engineer", "Data Analyst", "Graphic Designer", "Teacher",
        "Nurse", "Accountant", "Journalist", "Photographer", "Marketing Manager",
        "Project Manager", "Architect", "Chef", "Electrician", "Lawyer",
        "Pharmacist", "Physiotherapist", "Researcher", "Sales Manager",
        "Social Worker", "Veterinarian", "Content Creator", "UX Designer",
        "DevOps Engineer", "Security Analyst", "Product Manager", "Translator",
    )

    private val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    private val streetNames = listOf(
        "Maple", "Oak", "Pine", "Cedar", "Elm", "Birch", "Willow", "Ash",
        "Poplar", "Chestnut", "Beech", "Sycamore", "Hazel", "Rowan", "Linden",
    )

    private val streetTypes = listOf("Street", "Avenue", "Road", "Lane", "Drive", "Close", "Way", "Place", "Crescent")

    private val citiesWithCountry = listOf(
        "Portland" to ("OR 97201" to "United States"),
        "Denver" to ("CO 80203" to "United States"),
        "Austin" to ("TX 73301" to "United States"),
        "Seattle" to ("WA 98101" to "United States"),
        "Bristol" to ("BS1 4QA" to "United Kingdom"),
        "Edinburgh" to ("EH1 1TG" to "United Kingdom"),
        "Manchester" to ("M1 2GH" to "United Kingdom"),
        "Melbourne" to ("VIC 3000" to "Australia"),
        "Brisbane" to ("QLD 4000" to "Australia"),
        "Toronto" to ("ON M5V 2T6" to "Canada"),
        "Vancouver" to ("BC V6B 1A1" to "Canada"),
        "Berlin" to ("10115" to "Germany"),
        "Hamburg" to ("20095" to "Germany"),
        "Amsterdam" to ("1012 JS" to "Netherlands"),
        "Stockholm" to ("111 29" to "Sweden"),
        "Copenhagen" to ("1050" to "Denmark"),
        "Zurich" to ("8001" to "Switzerland"),
        "Vienna" to ("1010" to "Austria"),
        "Brussels" to ("1000" to "Belgium"),
        "Dublin" to ("D01 W5X4" to "Ireland"),
    )

    private val bioTemplates = listOf(
        "{name} works as a {occupation} and enjoys hiking and photography in their spare time. They moved to {city} three years ago and have never looked back. {pronoun} describes themselves as an introvert who loves a good book and strong coffee.",
        "A passionate {occupation}, {name} has spent years building expertise in their field. Based in {city}, {pronoun} spends weekends exploring local markets and experimenting with cooking. Privacy and digital autonomy are core values for {pronoun}.",
        "{name} is a {occupation} who values simplicity and authenticity. Originally from a small town, {pronoun} relocated to {city} for work and fell in love with the city's energy. {pronoun} is a keen cyclist and occasional blogger.",
        "After a decade in corporate life, {name} transitioned into {occupation} and hasn't looked back. {pronoun} lives in {city} with two cats and a growing collection of houseplants. Travel and language-learning are top hobbies.",
        "{name} calls {city} home and works as a {occupation}. Outside of work, {pronoun} volunteers at a local community garden and is an avid reader of science fiction. {pronoun} values privacy, open-source software, and good espresso.",
    )

    private val countryDialCodes = mapOf(
        "United States" to "+1",
        "United Kingdom" to "+44",
        "Australia" to "+61",
        "Canada" to "+1",
        "Germany" to "+49",
        "Netherlands" to "+31",
        "Sweden" to "+46",
        "Denmark" to "+45",
        "Switzerland" to "+41",
        "Austria" to "+43",
        "Belgium" to "+32",
        "Ireland" to "+353",
    )

    fun generate(): Alias {
        val firstName = firstNames[rng.nextInt(firstNames.size)]
        val lastName = lastNames[rng.nextInt(lastNames.size)]
        val name = AliasName(firstName, lastName)

        val dob = randomDob()
        val starSign = starSignFromDate(dob)

        val nationality = nationalities[rng.nextInt(nationalities.size)]
        val occupation = occupations[rng.nextInt(occupations.size)]
        val bloodType = bloodTypes[rng.nextInt(bloodTypes.size)]

        val (city, postcodeAndCountry) = citiesWithCountry[rng.nextInt(citiesWithCountry.size)]
        val (postcode, country) = postcodeAndCountry
        val streetNum = rng.nextInt(299) + 1
        val streetName = streetNames[rng.nextInt(streetNames.size)]
        val streetType = streetTypes[rng.nextInt(streetTypes.size)]
        val address = AliasAddress(
            street = "$streetNum $streetName $streetType",
            city = city,
            postcode = postcode,
            country = country,
        )

        val dialCode = countryDialCodes[country] ?: "+1"
        val phoneNumber = generatePhoneNumber(dialCode)

        val pronoun = if (rng.nextBoolean()) "They" else if (rng.nextBoolean()) "She" else "He"
        val bio = bioTemplates[rng.nextInt(bioTemplates.size)]
            .replace("{name}", firstName)
            .replace("{occupation}", occupation.lowercase())
            .replace("{city}", city)
            .replace("{pronoun}", pronoun)

        val accentColor = aliasAccentColors[rng.nextInt(aliasAccentColors.size)]

        val alias = Alias(
            name = name,
            dateOfBirth = dob,
            nationality = nationality,
            address = address,
            phoneNumber = phoneNumber,
            occupation = occupation,
            starSign = starSign,
            bloodType = bloodType,
            bio = bio,
            photoPath = null,
            accentColorInt = accentColor,
        )

        val accounts = accountSuggester.suggestAccounts(alias.id, name, phoneNumber)
        return alias.copy(accounts = accounts)
    }

    private fun randomDob(): String {
        val today = LocalDate.now()
        val minAge = 18L
        val maxAge = 60L
        val daysRange = (maxAge - minAge) * 365
        val offsetDays = (rng.nextLong() % daysRange).let { if (it < 0) -it else it }
        val dob = today.minusYears(minAge).minusDays(offsetDays)
        return dob.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun generatePhoneNumber(dialCode: String): String {
        val number = (1..9).joinToString("") { rng.nextInt(10).toString() }
        return "$dialCode $number"
    }

    private fun starSignFromDate(isoDate: String): String {
        val date = LocalDate.parse(isoDate)
        val month = date.monthValue
        val day = date.dayOfMonth
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Aries"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Taurus"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Gemini"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Cancer"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Leo"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Virgo"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Libra"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Scorpio"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Sagittarius"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Capricorn"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Aquarius"
            else -> "Pisces"
        }
    }
}
