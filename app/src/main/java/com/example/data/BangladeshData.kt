package com.example.data

object BangladeshData {
    val divisions: List<String> = listOf(
        "Dhaka",
        "Chattogram",
        "Rajshahi",
        "Khulna",
        "Barishal",
        "Sylhet",
        "Rangpur",
        "Mymensingh"
    )

    val districtsByDivision: Map<String, List<String>> = mapOf(
        "Dhaka" to listOf(
            "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur",
            "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari",
            "Shariatpur", "Tangail"
        ),
        "Chattogram" to listOf(
            "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Comilla",
            "Cox's Bazar", "Feni", "Khagrachhari", "Lakshmipur", "Noakhali", "Rangamati"
        ),
        "Rajshahi" to listOf(
            "Bogura", "Chapainawabganj", "Joypurhat", "Naogaon", "Natore",
            "Pabna", "Rajshahi", "Sirajganj"
        ),
        "Khulna" to listOf(
            "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna",
            "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira"
        ),
        "Barishal" to listOf(
            "Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur"
        ),
        "Sylhet" to listOf(
            "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"
        ),
        "Rangpur" to listOf(
            "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari",
            "Panchagarh", "Rangpur", "Thakurgaon"
        ),
        "Mymensingh" to listOf(
            "Jamalpur", "Mymensingh", "Netrokona", "Sherpur"
        )
    )

    val specialties: List<String> = listOf(
        "ICU & Critical Care",
        "Emergency & Trauma",
        "Maternal & Midwifery",
        "Pediatrics & NICU",
        "Cardiology & CCU",
        "Surgery & OT",
        "General Medicine",
        "Oncology & Palliative",
        "Orthopedics"
    )

    val designations: List<String> = listOf(
        "Senior Staff Nurse",
        "Nursing Supervisor",
        "Nursing Officer",
        "Staff Nurse",
        "Certified Midwife",
        "ICU Specialist Nurse",
        "OT Nurse",
        "Community Health Nurse"
    )

    val qualifications: List<String> = listOf(
        "B.Sc in Nursing",
        "Diploma in Nursing & Midwifery",
        "Post-Basic B.Sc in Nursing",
        "M.Sc in Nursing",
        "Diploma in Midwifery"
    )

    val bloodGroups: List<String> = listOf(
        "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
    )
}
