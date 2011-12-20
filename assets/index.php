<?php
	mysql_connect("mysql.studioblackdog.com", "vvz", "h3yh3ym4m4");
	mysql_select_db("android_pesquisa");
	
	$sql = "INSERT INTO answers VALUES (NULL, '";
	$sql .= join("', '", $_POST);
	$sql .= "');";
	
	mysql_query($sql);
	
	error_log("\n\n".$sql, 3, "/home/porkaria/studioblackdog.com/pesquisa/error.log");
	
	if ($_GET['view']) {
		$res = mysql_query("SELECT * FROM questions");
		while ($r = mysql_fetch_array($res)) {
			$questions[$r['id']] = array('title' => $r['title']) + explode(";", $r['options']);
		}		
		//echo "<pre>";print_r($questions);echo "</pre>";

		$res = mysql_query("SELECT * FROM answers");
		$index = 0;
		while ($r = mysql_fetch_array($res)) {
			foreach ($r as $k => $_answers) {
				$tmp = array();
				if (substr($k, 0, 8) == "question") {
					$question_id = substr($k, 8);
					$all_answers = explode(",", $_answers);
					foreach ($all_answers as $_answer) {
						$answer = explode("=", trim($_answer));
						$tmp[$answer[0]] = $questions[$question_id][$answer[0]] . "(".$answer[1].")";
					}
					$answers[$index][$k] = $tmp;
				}
			}
			$index++;				
		}
		//echo "<pre>";print_r($answers);echo "</pre>";
		
		?>
		<table>
		<?
		echo "<tr><td>&nbsp</td>";
		foreach ($questions as $k => $v) {
			echo "<td>".$k."</td>";
		}
		echo "</tr>";
		foreach ($answers as $k => $answer) {
			echo "<tr><td>".$k."</td>";			
			foreach ($answer as $q => $a) {
				//echo "<pre>";print_r($a);echo "</pre>";
				echo "<td>".join(", ", $a)."</td>";
			}
			echo "</tr>";
		}
		?>
		</table>
		<?
	}
?>