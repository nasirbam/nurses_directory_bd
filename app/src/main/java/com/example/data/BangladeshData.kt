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

    val upazilasByDistrict: Map<String, List<String>> = mapOf(
        // Dhaka Division
        "Dhaka" to listOf("Shahbagh", "Dhanmondi", "Mirpur", "Uttara", "Savar", "Dhamrai", "Keraniganj", "Nawabganj", "Dohar"),
        "Faridpur" to listOf("Faridpur Sadar", "Alfadanga", "Bhanga", "Boalmari", "Charbhadrasan", "Madhukhali", "Nagarkanda", "Sadarpur"),
        "Gazipur" to listOf("Gazipur Sadar", "Kaliakair", "Kaliganj", "Kapasia", "Sreepur"),
        "Gopalganj" to listOf("Gopalganj Sadar", "Kashiani", "Kotalipara", "Muksudpur", "Tungipara"),
        "Kishoreganj" to listOf("Kishoreganj Sadar", "Bajitpur", "Bhairab", "Hossainpur", "Itna", "Karimganj", "Katiadi", "Kuliarchar", "Pakundia"),
        "Madaripur" to listOf("Madaripur Sadar", "Kalkini", "Rajoir", "Shibchar", "Dasar"),
        "Manikganj" to listOf("Manikganj Sadar", "Daulatpur", "Ghior", "Harirampur", "Saturia", "Shivalaya", "Singair"),
        "Munshiganj" to listOf("Munshiganj Sadar", "Gazaria", "Lohajang", "Sirajdikhan", "Sreenagar", "Tongibari"),
        "Narayanganj" to listOf("Narayanganj Sadar", "Araihazar", "Bandar", "Rupganj", "Sonargaon"),
        "Narsingdi" to listOf("Narsingdi Sadar", "Belabo", "Monohardi", "Palash", "Raipura", "Shibpur"),
        "Rajbari" to listOf("Rajbari Sadar", "Baliakandi", "Goalandaghat", "Kalukhali", "Pangsha"),
        "Shariatpur" to listOf("Shariatpur Sadar", "Bhedarganj", "Damudya", "Gosairhat", "Naria", "Zajira"),
        "Tangail" to listOf("Tangail Sadar", "Basail", "Bhuapur", "Delduar", "Ghatail", "Gopalpur", "Kalihati", "Madhupur", "Mirzapur", "Sakhipur"),

        // Chattogram Division
        "Bandarban" to listOf("Bandarban Sadar", "Alikadam", "Lama", "Naikhongchhari", "Rowangchhari", "Ruma", "Thanchi"),
        "Brahmanbaria" to listOf("Brahmanbaria Sadar", "Akhaura", "Ashuganj", "Bancharampur", "Kasba", "Nabinagar", "Nasirnagar", "Sarail"),
        "Chandpur" to listOf("Chandpur Sadar", "Faridganj", "Haimchar", "Haziganj", "Kachua", "Matlab Dakshin", "Matlab Uttar", "Shahrasti"),
        "Chattogram" to listOf("Panchlaish", "Kotwali", "Anwara", "Banshkhali", "Boalkhali", "Chandanaish", "Fatikchhari", "Hathazari", "Lohagara", "Mirsharai", "Patiya", "Rangunia", "Raozan", "Sandwip", "Satkania", "Sitakunda"),
        "Comilla" to listOf("Comilla Adarsha Sadar", "Barura", "Brahmanpara", "Burichang", "Chandina", "Chauddagram", "Daudkandi", "Debidwar", "Homna", "Laksam", "Muradnagar", "Nangalkot"),
        "Cox's Bazar" to listOf("Cox's Bazar Sadar", "Chakaria", "Eidgaon", "Kutubdia", "Maheshkhali", "Pekua", "Ramu", "Teknaf", "Ukhia"),
        "Feni" to listOf("Feni Sadar", "Chhagalnaiya", "Daganbhuiyan", "Fulgazi", "Parshuram", "Sonagazi"),
        "Khagrachhari" to listOf("Khagrachhari Sadar", "Dighinala", "Guimara", "Lakshmichhari", "Mahalchhari", "Manikchhari", "Matiranga", "Panchhari", "Ramgarh"),
        "Lakshmipur" to listOf("Lakshmipur Sadar", "Kamalnagar", "Raipur", "Ramganj", "Ramgati"),
        "Noakhali" to listOf("Noakhali Sadar", "Begumganj", "Chatkhil", "Companiganj", "Hatiya", "Kabirhat", "Senbagh", "Sonaimuri", "Subarnachar"),
        "Rangamati" to listOf("Rangamati Sadar", "Baghaichhari", "Barkal", "Belaichhari", "Juraichhari", "Kaptai", "Kawkhali", "Langadu", "Naniarchar", "Rajasthali"),

        // Rajshahi Division
        "Bogura" to listOf("Bogura Sadar", "Adamdighi", "Dhunat", "Dhupchanchia", "Gabtali", "Kahaloo", "Nandigram", "Sariakandi", "Shajahanpur", "Sherpur", "Shibganj", "Sonatola"),
        "Chapainawabganj" to listOf("Chapainawabganj Sadar", "Bholahat", "Gomastapur", "Nachole", "Shibganj"),
        "Joypurhat" to listOf("Joypurhat Sadar", "Akkelpur", "Kalai", "Khetlal", "Panchbibi"),
        "Naogaon" to listOf("Naogaon Sadar", "Atrai", "Badalgachhi", "Dhamoirhat", "Manda", "Mahadevpur", "Niamatpur", "Patnitala", "Porsha", "Raninagar", "Sapahar"),
        "Natore" to listOf("Natore Sadar", "Bagatipara", "Baraigram", "Gurudaspur", "Lalpur", "Naldanga", "Singra"),
        "Pabna" to listOf("Pabna Sadar", "Atgharia", "Bera", "Bhangura", "Chatmohar", "Faridpur", "Ishwardi", "Santhia", "Sujanagar"),
        "Rajshahi" to listOf("Rajpara", "Boalia", "Bagha", "Bagmara", "Charghat", "Durgapur", "Godagari", "Mohanpur", "Paba", "Puthia", "Tanore"),
        "Sirajganj" to listOf("Sirajganj Sadar", "Belkuchi", "Chauhali", "Kamarkhanda", "Kazipur", "Raiganj", "Shahjadpur", "Tarash", "Ullahpara"),

        // Khulna Division
        "Bagerhat" to listOf("Bagerhat Sadar", "Chitalmari", "Fakirhat", "Kachua", "Mollahat", "Mongla", "Morrelganj", "Rampal", "Sarankhola"),
        "Chuadanga" to listOf("Chuadanga Sadar", "Alamdanga", "Damurhuda", "Jibannagar"),
        "Jashore" to listOf("Jashore Sadar", "Abhaynagar", "Bagherpara", "Chaugachha", "Jhikargachha", "Keshabpur", "Manirampur", "Sharsha"),
        "Jhenaidah" to listOf("Jhenaidah Sadar", "Harinakunda", "Kaliganj", "Kotchandpur", "Maheshpur", "Shailkupa"),
        "Khulna" to listOf("Sonadanga", "Khalishpur", "Batiaghata", "Dacope", "Dumuria", "Dighalia", "Koyra", "Paikgachha", "Phultala", "Rupsha", "Terokhada"),
        "Kushtia" to listOf("Kushtia Sadar", "Bheramara", "Daulatpur", "Khoksa", "Kumarkhali", "Mirpur"),
        "Magura" to listOf("Magura Sadar", "Mohammadpur", "Shalikha", "Sreepur"),
        "Meherpur" to listOf("Meherpur Sadar", "Gangni", "Mujibnagar"),
        "Narail" to listOf("Narail Sadar", "Kalia", "Lohagara"),
        "Satkhira" to listOf("Satkhira Sadar", "Assasuni", "Debhata", "Kalaroa", "Kaliganj", "Shyamnagar", "Tala"),

        // Barishal Division
        "Barguna" to listOf("Barguna Sadar", "Amtali", "Bamna", "Betagi", "Patharghata", "Taltali"),
        "Barishal" to listOf("Barishal Sadar", "Agailjhara", "Babuganj", "Bakerganj", "Banaripara", "Gaurnadi", "Hizla", "Mehendiganj", "Muladi", "Wazirpur"),
        "Bhola" to listOf("Bhola Sadar", "Burhanuddin", "Char Fasson", "Daulatkhan", "Lalmohan", "Manpura", "Tazumuddin"),
        "Jhalokati" to listOf("Jhalokati Sadar", "Kathalia", "Nalchity", "Rajapur"),
        "Patuakhali" to listOf("Patuakhali Sadar", "Bauphal", "Dashmina", "Dumki", "Galachipa", "Kalapara", "Mirzaganj", "Rangabali"),
        "Pirojpur" to listOf("Pirojpur Sadar", "Bhandaria", "Kawkhali", "Mathbaria", "Nazirpur", "Nesarabad", "Indurkani"),

        // Sylhet Division
        "Habiganj" to listOf("Habiganj Sadar", "Ajmiriganj", "Bahubal", "Baniyachong", "Chunarughat", "Lakhai", "Madhabpur", "Nabiganj", "Shaistaganj"),
        "Moulvibazar" to listOf("Moulvibazar Sadar", "Barlekha", "Juri", "Kamalganj", "Kulaura", "Rajnagar", "Sreemangal"),
        "Sunamganj" to listOf("Sunamganj Sadar", "Bishwamvarpur", "Chhatak", "Dakshin Sunamganj", "Derai", "Dharamapasha", "Dowarabazar", "Jagannathpur", "Jamalganj", "Sullah", "Tahirpur"),
        "Sylhet" to listOf("Sylhet Sadar", "Balaganj", "Beanibazar", "Bishwanath", "Companiganj", "Dakshin Surma", "Fenchuganj", "Golapganj", "Gowainghat", "Jaintiapur", "Kanaighat", "Osmani Nagar", "Zakiganj"),

        // Rangpur Division
        "Dinajpur" to listOf("Dinajpur Sadar", "Birampur", "Birganj", "Biral", "Bochaganj", "Chirirbandar", "Fulbari", "Ghoraghat", "Hakimpur", "Kaharole", "Khansama", "Nawabganj", "Parbatipur"),
        "Gaibandha" to listOf("Gaibandha Sadar", "Fulchhari", "Gobindaganj", "Palashbari", "Sadullapur", "Saghata", "Sundarganj"),
        "Kurigram" to listOf("Kurigram Sadar", "Bhurungamari", "Char Rajibpur", "Chilmari", "Nageshwari", "Phulbari", "Rajarhat", "Raomari", "Ulipur"),
        "Lalmonirhat" to listOf("Lalmonirhat Sadar", "Aditmari", "Hatibandha", "Kaliganj", "Patgram"),
        "Nilphamari" to listOf("Nilphamari Sadar", "Dimla", "Domar", "Jaldhaka", "Kishoreganj", "Saidpur"),
        "Panchagarh" to listOf("Panchagarh Sadar", "Atwari", "Boda", "Debiganj", "Tetulia"),
        "Rangpur" to listOf("Rangpur Sadar", "Badarganj", "Gangachhara", "Kaunia", "Mithapukur", "Pirgachha", "Pirganj", "Taraganj"),
        "Thakurgaon" to listOf("Thakurgaon Sadar", "Baliadangi", "Haripur", "Pirganj", "Ranisankail"),

        // Mymensingh Division
        "Jamalpur" to listOf("Jamalpur Sadar", "Baksiganj", "Dewanganj", "Islampur", "Madarganj", "Melandaha", "Sarishabari"),
        "Mymensingh" to listOf("Mymensingh Sadar", "Bhaluka", "Dhobaura", "Fulbaria", "Gaffargaon", "Gauripur", "Haluaghat", "Ishwarganj", "Muktagachha", "Nandail", "Phulpur", "Tarakanda", "Trishal"),
        "Netrokona" to listOf("Netrokona Sadar", "Atpara", "Barhatta", "Durgapur", "Kalmakanda", "Kendua", "Khaliajuri", "Madan", "Mohanganj", "Purbadhala"),
        "Sherpur" to listOf("Sherpur Sadar", "Jhenaigati", "Nakla", "Nalitabari", "Sreebardi")
    )

    fun upazilasForDistrict(district: String): List<String> {
        return upazilasByDistrict[district] ?: listOf("$district Sadar")
    }

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
