local index = 0
local result = 0
local keyLength = #KEYS
for i = 1, keyLength do
	local flag = redis.call("set",KEYS[i],"1","NX","EX",5);
	if(flag) then
		index = index + 1
	else
		result = result + 1
		break;
	end
end
if result == 1 then
	for i = 1,index,1 do
        redis.call("del",KEYS[i]);
    end
end
return result