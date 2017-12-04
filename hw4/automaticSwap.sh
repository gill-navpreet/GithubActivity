#/bin/bash
# This shell script does automatic swapping given only the name of the docker image to swap to

display_usage() {
    echo -e "\nUsage:\n ./automaticSwap <image name to be swapped to> \n"
}


# Function to remove existing containers
function killitif {
    docker ps -a  > /tmp/kill$$
    if grep -q $1 /tmp/kill$$
     then
        echo "KILLING: $1"
        docker rm -f `docker ps -a | grep $1  | sed -e 's: .*$::'`
   fi
}


#User didn't provide the argument
if [ $# -eq 0 ]
 then
    display_usage
    exit 1
fi

# Save the output to a temp file
docker ps > /tmp/web$$

# grep for web2 container name
if grep -q web2 /tmp/web$$
 then
    echo "------ HOTSWAPPING TO WEB1 ------"
    # kill web1 if already running
    killitif web1
    sleep 10 && docker run --network ecs189_default --name web1 $1 &
    echo "HOTSWAP REQUEST --> nginx "
    sleep 25 && docker exec ecs189_proxy_1 /bin/bash /bin/swap1.sh
    if [ $? -eq 1 ]
    then
        echo "TRYING AGAIN ....."
        sleep 25 && docker exec ecs189_proxy_1 /bin/bash /bin/swap2.sh
    fi
    echo "STARTED: New Server " $1
    echo "Redirecting..."
    # delete old container because only one can be running as per HW specs
    killitif web2
    echo "REMOVED: ecs189_web2_1"
fi


# grep for web1 container name
if grep -q web1 /tmp/web$$ # don't produce any output
 then
    echo "------ HOTSWAPPING TO WEB2 ------"
    # kill web2 if already running
    killitif web2
    sleep 10 && docker run --network ecs189_default --name web2 $1 &
    echo "HOTSWAP REQUEST --> nginx "
    sleep 25 && docker exec ecs189_proxy_1 /bin/bash /bin/swap2.sh
    if [ $? -eq 1 ]
    then
        echo "TRYING AGAIN ....."
        sleep 25 && docker exec ecs189_proxy_1 /bin/bash /bin/swap2.sh
    fi
    echo "STARTED: New Server " $1
    echo "Redirecting..."
    # delete old container because only one can be running as per HW specs
    killitif web1
    echo "REMOVED: ecs189_web1_1"
fi

# remove tmp files created on exit
trap "rm -f /tmp/web* /tmp/kill*" EXIT

