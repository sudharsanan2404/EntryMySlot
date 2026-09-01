package com.entrymyslot.app.data

/** Offline city catalogue used by the location picker. */
object IndiaCities {
    val all: List<String> = listOf(
        // Andaman and Nicobar Islands
        "Bamboo Flat", "Car Nicobar", "Diglipur", "Mayabunder", "Port Blair", "Rangat",
        // Andhra Pradesh
        "Adoni", "Amalapuram", "Anakapalle", "Anantapur", "Bapatla", "Bhimavaram",
        "Chilakaluripet", "Chittoor", "Dharmavaram", "Eluru", "Gooty", "Gudivada",
        "Guntakal", "Guntur", "Hindupur", "Kadapa", "Kadiri", "Kakinada", "Kavali",
        "Kurnool", "Machilipatnam", "Madanapalle", "Mangalagiri", "Markapur", "Nandyal",
        "Narasaraopet", "Nellore", "Ongole", "Palakollu", "Proddatur", "Rajahmundry",
        "Rayachoti", "Srikakulam", "Tadepalligudem", "Tadipatri", "Tenali", "Tirupati",
        "Vijayawada", "Visakhapatnam", "Vizianagaram",
        // Arunachal Pradesh
        "Aalo", "Bomdila", "Itanagar", "Naharlagun", "Namsai", "Pasighat", "Roing", "Tawang", "Tezu", "Ziro",
        // Assam
        "Barpeta", "Bongaigaon", "Dhubri", "Dibrugarh", "Diphu", "Goalpara", "Golaghat",
        "Guwahati", "Haflong", "Hailakandi", "Jorhat", "Karimganj", "Kokrajhar", "Lakhimpur",
        "Mangaldoi", "Nagaon", "Nalbari", "North Lakhimpur", "Silchar", "Sivasagar", "Tezpur", "Tinsukia",
        // Bihar
        "Araria", "Arrah", "Aurangabad", "Bagaha", "Begusarai", "Bettiah", "Bhagalpur",
        "Bihar Sharif", "Buxar", "Chhapra", "Darbhanga", "Dehri", "Gaya", "Hajipur",
        "Jamalpur", "Jehanabad", "Katihar", "Kishanganj", "Lakhisarai", "Madhubani", "Motihari",
        "Munger", "Muzaffarpur", "Nawada", "Patna", "Purnia", "Saharsa", "Samastipur",
        "Sasaram", "Sitamarhi", "Siwan", "Supaul",
        // Chandigarh
        "Chandigarh",
        // Chhattisgarh
        "Ambikapur", "Bhilai", "Bilaspur", "Chirmiri", "Dalli-Rajhara", "Dhamtari", "Durg",
        "Jagdalpur", "Janjgir", "Kanker", "Kawardha", "Korba", "Mahasamund", "Raigarh",
        "Raipur", "Rajnandgaon",
        // Dadra and Nagar Haveli and Daman and Diu
        "Daman", "Diu", "Silvassa",
        // Delhi
        "Delhi", "New Delhi",
        // Goa
        "Bicholim", "Canacona", "Mapusa", "Margao", "Panaji", "Ponda", "Vasco da Gama",
        // Gujarat
        "Ahmedabad", "Amreli", "Anand", "Anjar", "Bharuch", "Bhavnagar", "Bhuj", "Botad",
        "Dahod", "Deesa", "Gandhidham", "Gandhinagar", "Godhra", "Himatnagar", "Jamnagar",
        "Junagadh", "Kalol", "Mehsana", "Morbi", "Nadiad", "Navsari", "Palanpur", "Patan",
        "Porbandar", "Rajkot", "Surat", "Surendranagar", "Vadodara", "Valsad", "Vapi", "Veraval",
        // Haryana
        "Ambala", "Bahadurgarh", "Bhiwani", "Faridabad", "Fatehabad", "Gurugram", "Hisar",
        "Jind", "Kaithal", "Karnal", "Kurukshetra", "Narnaul", "Palwal", "Panchkula", "Panipat",
        "Rewari", "Rohtak", "Sirsa", "Sonipat", "Yamunanagar",
        // Himachal Pradesh
        "Baddi", "Bilaspur", "Chamba", "Dalhousie", "Dharamshala", "Hamirpur", "Kangra",
        "Kullu", "Mandi", "Manali", "Nahan", "Palampur", "Shimla", "Solan", "Una",
        // Jammu and Kashmir
        "Anantnag", "Baramulla", "Jammu", "Kathua", "Kishtwar", "Poonch", "Pulwama", "Rajouri",
        "Sopore", "Srinagar", "Udhampur",
        // Jharkhand
        "Bokaro", "Chaibasa", "Chatra", "Deoghar", "Dhanbad", "Dumka", "Giridih", "Godda",
        "Hazaribagh", "Jamshedpur", "Jhumri Telaiya", "Medininagar", "Ramgarh", "Ranchi", "Sahibganj",
        // Karnataka
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru", "Bhadravati", "Bidar", "Chikkaballapur",
        "Chikkamagaluru", "Chitradurga", "Davanagere", "Gadag", "Gangavati", "Hassan", "Haveri",
        "Hosapete", "Hubballi", "Kalaburagi", "Karwar", "Kolar", "Koppal", "Madikeri", "Mandya",
        "Mangaluru", "Mysuru", "Raichur", "Ramanagara", "Ranebennuru", "Shivamogga", "Tumakuru",
        "Udupi", "Vijayapura", "Yadgir",
        // Kerala
        "Alappuzha", "Aluva", "Attingal", "Changanassery", "Cherthala", "Guruvayur", "Idukki",
        "Kannur", "Kasaragod", "Kayamkulam", "Kochi", "Kollam", "Kottayam", "Kozhikode",
        "Malappuram", "Manjeri", "Munnar", "Neyyattinkara", "Nilambur", "Ottapalam", "Palakkad",
        "Pathanamthitta", "Payyanur", "Perinthalmanna", "Ponnani", "Thalassery", "Thiruvananthapuram",
        "Thodupuzha", "Thrissur", "Tirur", "Varkala",
        // Ladakh
        "Kargil", "Leh",
        // Lakshadweep
        "Agatti", "Andrott", "Kavaratti", "Minicoy",
        // Madhya Pradesh
        "Betul", "Bhind", "Bhopal", "Burhanpur", "Chhatarpur", "Chhindwara", "Damoh", "Datia",
        "Dewas", "Dhar", "Guna", "Gwalior", "Hoshangabad", "Indore", "Itarsi", "Jabalpur",
        "Katni", "Khandwa", "Khargone", "Mandsaur", "Morena", "Neemuch", "Pithampur", "Ratlam",
        "Rewa", "Sagar", "Satna", "Sehore", "Seoni", "Shahdol", "Shivpuri", "Singrauli", "Ujjain", "Vidisha",
        // Maharashtra
        "Ahmednagar", "Akola", "Amravati", "Aurangabad", "Baramati", "Beed", "Bhandara", "Bhiwandi",
        "Bhusawal", "Chandrapur", "Dhule", "Gondia", "Ichalkaranji", "Jalgaon", "Jalna", "Kalyan",
        "Karad", "Kolhapur", "Latur", "Malegaon", "Mira-Bhayandar", "Mumbai", "Nagpur", "Nanded",
        "Nandurbar", "Nashik", "Navi Mumbai", "Osmanabad", "Palghar", "Panvel", "Parbhani", "Pimpri-Chinchwad",
        "Pune", "Ratnagiri", "Sangli", "Satara", "Solapur", "Thane", "Ulhasnagar", "Vasai-Virar", "Wardha", "Yavatmal",
        // Manipur
        "Bishnupur", "Churachandpur", "Imphal", "Kakching", "Senapati", "Thoubal", "Ukhrul",
        // Meghalaya
        "Jowai", "Nongpoh", "Shillong", "Tura", "Williamnagar",
        // Mizoram
        "Aizawl", "Champhai", "Kolasib", "Lunglei", "Saiha", "Serchhip",
        // Nagaland
        "Chumukedima", "Dimapur", "Kohima", "Mokokchung", "Mon", "Tuensang", "Wokha", "Zunheboto",
        // Odisha
        "Angul", "Balangir", "Balasore", "Barbil", "Bargarh", "Baripada", "Berhampur", "Bhadrak",
        "Bhubaneswar", "Boudh", "Cuttack", "Dhenkanal", "Jajpur", "Jeypore", "Jharsuguda", "Kendrapara",
        "Keonjhar", "Koraput", "Paradip", "Puri", "Rayagada", "Rourkela", "Sambalpur",
        // Puducherry
        "Karaikal", "Mahe", "Puducherry", "Yanam",
        // Punjab
        "Abohar", "Amritsar", "Barnala", "Batala", "Bathinda", "Firozpur", "Hoshiarpur", "Jalandhar",
        "Kapurthala", "Khanna", "Ludhiana", "Malerkotla", "Moga", "Mohali", "Pathankot", "Patiala",
        "Phagwara", "Rajpura", "Rupnagar", "Sangrur",
        // Rajasthan
        "Ajmer", "Alwar", "Banswara", "Baran", "Barmer", "Beawar", "Bharatpur", "Bhilwara",
        "Bikaner", "Bundi", "Chittorgarh", "Churu", "Dausa", "Dholpur", "Hanumangarh", "Jaipur",
        "Jaisalmer", "Jalore", "Jhalawar", "Jhunjhunu", "Jodhpur", "Kishangarh", "Kota", "Nagaur",
        "Pali", "Pushkar", "Sawai Madhopur", "Sikar", "Sri Ganganagar", "Tonk", "Udaipur",
        // Sikkim
        "Gangtok", "Gyalshing", "Mangan", "Namchi", "Rangpo", "Singtam",
        // Tamil Nadu
        "Ambur", "Arakkonam", "Ariyalur", "Avadi", "Chengalpattu", "Chennai", "Coimbatore", "Coonoor",
        "Cuddalore", "Dharmapuri", "Dindigul", "Erode", "Gudiyatham", "Hosur", "Kanchipuram", "Karaikudi",
        "Karur", "Kodaikanal", "Kumbakonam", "Madurai", "Mayiladuthurai", "Nagapattinam", "Nagercoil",
        "Namakkal", "Neyveli", "Ooty", "Palani", "Perambalur", "Pollachi", "Pudukkottai", "Rajapalayam",
        "Ramanathapuram", "Ranipet", "Salem", "Sivakasi", "Tambaram", "Thanjavur", "Theni", "Thoothukudi",
        "Tiruchirappalli", "Tirunelveli", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur", "Vellore", "Viluppuram", "Virudhunagar",
        // Telangana
        "Adilabad", "Bhongir", "Bodhan", "Gadwal", "Hyderabad", "Jagtial", "Jangaon", "Kamareddy",
        "Karimnagar", "Khammam", "Kothagudem", "Mahabubabad", "Mahbubnagar", "Mancherial", "Medak",
        "Miryalaguda", "Nalgonda", "Nirmal", "Nizamabad", "Ramagundam", "Sangareddy", "Siddipet", "Suryapet", "Vikarabad", "Warangal",
        // Tripura
        "Agartala", "Belonia", "Dharmanagar", "Kailasahar", "Khowai", "Udaipur",
        // Uttar Pradesh
        "Agra", "Aligarh", "Allahabad", "Ambedkar Nagar", "Amroha", "Auraiya", "Ayodhya", "Azamgarh",
        "Baghpat", "Bahraich", "Ballia", "Balrampur", "Banda", "Barabanki", "Bareilly", "Basti", "Bhadohi",
        "Bijnor", "Budaun", "Bulandshahr", "Chandauli", "Deoria", "Etah", "Etawah", "Farrukhabad",
        "Fatehpur", "Firozabad", "Ghaziabad", "Ghazipur", "Gonda", "Gorakhpur", "Greater Noida", "Hapur",
        "Hardoi", "Hathras", "Jaunpur", "Jhansi", "Kannauj", "Kanpur", "Kasganj", "Kaushambi", "Kushinagar",
        "Lakhimpur", "Lalitpur", "Lucknow", "Mainpuri", "Mathura", "Mau", "Meerut", "Mirzapur", "Moradabad",
        "Muzaffarnagar", "Noida", "Pilibhit", "Pratapgarh", "Raebareli", "Rampur", "Saharanpur", "Sambhal",
        "Shahjahanpur", "Shamli", "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Varanasi",
        // Uttarakhand
        "Almora", "Bageshwar", "Dehradun", "Haldwani", "Haridwar", "Kashipur", "Kotdwar", "Mussoorie",
        "Nainital", "Pithoragarh", "Rishikesh", "Roorkee", "Rudrapur", "Srinagar Garhwal",
        // West Bengal
        "Alipurduar", "Asansol", "Baharampur", "Balurghat", "Bankura", "Barasat", "Bardhaman", "Barrackpore",
        "Basirhat", "Bishnupur", "Bolpur", "Chandannagar", "Cooch Behar", "Darjeeling", "Durgapur", "Haldia",
        "Howrah", "Jalpaiguri", "Kharagpur", "Kolkata", "Krishnanagar", "Malda", "Medinipur", "Purulia",
        "Raiganj", "Siliguri", "Serampore"
    )
}
