/* Yeayeun Park
 * 10/3/2015
 * Trims company names and start fuzzy matching
 */ 
 
 
 *remove leading and trailing spaces
 replace company_name = strtrim(company_name)
 
 *remove special characters
# need a comprehensive character replacement list
 replace company_name = subinstr(company_name, "*", "",.) 
 replace company_name = subinstr(company_name, char(34), "",.) 
 replace company_name = subinstr(company_name, "'", "",.) 
 replace company_name = subinstr(company_name, " ", "",.) 

 *capitalize all letters
 replace company_name = upper(company_name)
 
 *create indicator variable for government-related agencies 
# use company_id not company_name
 gen gov = .
 #delim ;
 replace gov = 1 if !(regexm(company_name, "COÓ)|regexm(company_name, "INC")
 |regexm(company_name, "LTD")|regexm(company_name, "CORP")
 |regexm(company_name, "GEOGRAPHIC")|regexm(company_name, "COOP")
 |regexm(company_name, "LABTOOLS")|regexm(company_name, "READY MIXED")) 
 & (regexm(company_name, "FEDERAL")|regexm(company_name, "MINISTRY")
 |regexm(company_name, "PATENT OFFICE")|regexm(company_name, "NATIONAL") 
 |regexm(company_name, "NACIONAL")| regexm(company_name, "STATE AUTHORITY")
 ;
 #delim cr
 
 *take care of – (connects between full name and acronym)
 replace company_name = subinstr(company_name, "–", "-", .)
 
 *take care of those with "TIA"
 gen tia = .
 replace tia = (regexm(company_name, " TIA"))
 replace tia = 0 if substr(company_name,-3,.) != "TIA"
 replace company_name = subinstr(company_name, " TIA", "", .) if tia == 1 //remove "TIA" from names
 
 *arrange in alphabetical order
 gsort company_name
 list company_name
 
 *manual changes to individual variables with special characters
 replace company_name = "INTERSPOT DIS TIC. A.S." if company_name == "?NTERSPOR DIS TIC. A.S."
 
 *non-profit companies
 gen nonprof = .
 **** code***
 
 
 /****fuzzy matching*****/
 ssc install  

 *divide the file up into parts (due to memory limit)
 forval i = 1(500)98574{
  use "/Users/yeayeunpark/Desktop/stata/company_name.dta", clear
  keep if _n >= `i' & _n < `i'+500
  save "/Users/yeayeunpark/Desktop/stata/company_name_`i'.dta", replace
 }

 *create Levenstein distances
 forval i = 1(500)98501{
  use "/Users/yeayeunpark/Desktop/stata/company_name_`i'.dta", clear
  save "/Users/yeayeunpark/Desktop/stata/company_name_`i'_v2.dta", replace
  rename company_name company_name_v2
  cross using "/Users/yeayeunpark/Desktop/stata/company_name_`i'.dta"
  strdist company_name company_name_v2
  save "/Users/yeayeunpark/Desktop/stata/company_name_`i'_v2.dta", replace
 }
 
 /****fuzzy matching*****/

> str(sqldf("select * from entity_names where company_id_cleansed like '%university%'"))

> entity_names$is_university <- sqldf(c("update entity_names set is_university = 1 where company_id_cleansed = '%university%'","select is_university from main.entity_names"))

> write.csv(entity_names, file = "SortedCompanyId.csv")

