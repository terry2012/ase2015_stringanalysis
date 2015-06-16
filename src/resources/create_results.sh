#Assume you start in a folder with the two dirs populated
mkdir google_play_results
mkdir google_play_results/all_strings
mkdir google_play_results/intent_values
mkdir google_play_results/reflection_method_analysis
mkdir google_play_results/reflection_value_analysis
mkdir google_play_results/url_method_analysis
mkdir google_play_results/url_value_analysis
mkdir malgenome_results
mkdir malgenome_results/all_strings
mkdir malgenome_results/intent_values
mkdir malgenome_results/reflection_method_analysis
mkdir malgenome_results/reflection_value_analysis
mkdir malgenome_results/url_method_analysis
mkdir malgenome_results/url_value_analysis
cd googleplay
find . -name "*all_soot_strings.csv" -type f -exec cp {} ./../google_play_results/all_strings/ \;
find . -name "*intent_value_analysis.csv" -type f -exec cp {} ./../google_play_results/intent_values/ \;
find . -name "*refelction_method_analysis.csv" -type f -exec cp {} ./../google_play_results/reflection_method_analysis/ \;
find . -name "*reflection_value_analysis.csv" -type f -exec cp {} ./../google_play_results/reflection_value_analysis/ \;
find . -name "*url_method_analysis.csv" -type f -exec cp {} ./../google_play_results/url_method_analysis/ \;
find . -name "*url_value_analysis.csv" -type f -exec cp {} ./../google_play_results/url_value_analysis/ \;
cd ..
cd malgenome
find . -name "*all_soot_strings.csv" -type f -exec cp {} ./../malgenome_results/all_strings/ \;
find . -name "*intent_value_analysis.csv" -type f -exec cp {} ./../malgenome_results/intent_values/ \;
find . -name "*refelction_method_analysis.csv" -type f -exec cp {} ./../malgenome_results/reflection_method_analysis/ \;
find . -name "*reflection_value_analysis.csv" -type f -exec cp {} ./../malgenome_results/reflection_value_analysis/ \;
find . -name "*url_method_analysis.csv" -type f -exec cp {} ./../malgenome_results/url_method_analysis/ \;
find . -name "*url_value_analysis.csv" -type f -exec cp {} ./../malgenome_results/url_value_analysis/ \;
cd ..
cat google_play_results/all_strings/*.csv > gp_all_strings_merged.csv
cat google_play_results/intent_values/*.csv > gp_intent_values_merged.csv
cat google_play_results/reflection_method_analysis/*.csv > gp_ref_method_analysis_merged.csv
cat google_play_results/reflection_value_analysis/*.csv > gp_ref_value_analysis_merged.csv
cat google_play_results/url_method_analysis/*.csv > gp_url_method_analysis_merged.csv
cat google_play_results/url_value_analysis/*.csv > gp_url_value_analysis_merged.csv
cat malgenome_results/all_strings/*.csv > mal_all_strings_merged.csv 
cat malgenome_results/intent_values/*.csv > mal_intent_values_merged.csv 
cat malgenome_results/reflection_method_analysis/*.csv > mal_ref_method_analysis_merged.csv 
cat malgenome_results/reflection_value_analysis/*.csv > mal_ref_value_analysis_merged.csv 
cat malgenome_results/url_method_analysis/*.csv > mal_url_method_analysis_merged.csv 
cat malgenome_results/url_value_analysis/*.csv > mal_url_value_analysis_merged.csv
# create the summed up summaryies
#http://stackoverflow.com/questions/2314750/how-to-assign-the-output-of-a-shell-command-to-a-variable
PWD=`pwd`
FILE='/gp_intent_values_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/gp_ref_method_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/gp_ref_value_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/gp_url_method_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/gp_url_value_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/mal_intent_values_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/mal_ref_method_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/mal_ref_value_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/mal_url_method_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
FILE='/mal_url_value_analysis_merged.csv'
java -jar summary.jar $PWD$FILE
