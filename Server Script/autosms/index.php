<?php

	$response = array();
	
	$server = "localhost";
	$database = "test";
	$username = "root";
	$password = "";
	
	$conn = new mysqli($server, $username, $password, $database);
	if($conn->error){
		die("Connection Error ".$conn->error);
	}
	$query = "select * from sms where status = 0";
	
	$result = $conn->query($query);
	while($row = $result->fetch_assoc()){
		$response[] = $row;
	}
	echo json_encode($response);
	
	$query = "update sms set status = 1 where status = 0"; 
	$conn->query($query);
	
	
?>