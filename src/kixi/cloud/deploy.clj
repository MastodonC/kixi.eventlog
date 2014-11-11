(ns kixi.cloud.deploy
  (:require [amazonica.aws.ec2 :as ec2]
            [amazonica.aws.elasticloadbalancing :as elb]))



(defn build* [{:keys [vpc-cidr-block subnet-cidr-block security-group key-name instance-type min-count max-count tag load-balancer-name]}]
  (let [tags                [{:key "Name" :value tag}]
        vpcs                (ec2/create-vpc {:cidr-block vpc-cidr-block
                                             :tags       tag})
        vpc-id              (get-in vpcs [:vpc :vpc-id])
        route-tables        (ec2/describe-route-tables :filters [{:name "vpc-id" :values [vpc-id]}])
        route-table-id      (get-in route-tables [:route-tables 0 :route-table-id])
        internet-gateway    (ec2/create-internet-gateway {:vpc-id vpc-id
                                                          :cidr-block vpc-cidr-block
                                                          :tags       tag})
        internet-gateway-id (get-in internet-gateway [:internet-gateway :internet-gateway-id])
        subnet              (ec2/create-subnet {:cidr-block subnet-cidr-block
                                                :vpc-id vpc-id
                                                :tags       tag
                                                })
        subnet-id           (get-in subnet [:subnet :subnet-id])]

    (ec2/attach-internet-gateway {:internet-gateway-id internet-gateway-id :vpc-id vpc-id})
    (ec2/create-route {:route-table-id route-table-id :destination-cidr-block "0.0.0.0/0" :gateway-id internet-gateway-id})
    (ec2/modify-subnet-attribute {:subnet-id subnet-id
                                  :map-public-ip-on-launch true})
    (let [reservation (ec2/run-instances {:image-id       "ami-6e7bd919"
                                          :instance-type  instance-type
                                          :vpc-id         vpc-id
                                          :subnet-id      subnet-id
                                          :min-count      min-count
                                          :max-count      max-count
                                          :key-name       key-name
                                          :security-group security-group})
          elb         (elb/create-load-balancer {:load-balancer-name load-balancer-name
                                                 :subnets [subnet-id]
                                                 :listeners [{:instance-port 80
                                                              :load-balancer-port 80
                                                              :instance-protocol "HTTP"
                                                              :protocol "HTTP"}]})]
      (doseq [{:keys [instance-id]} (get-in reservation [:reservation :instances])]
        (elb/register-instances-with-load-balancer {:load-balancer-name load-balancer-name :instances [{:instance-id instance-id}]}))
      (merge vpcs internet-gateway subnet reservation))))

(defn build []
  (build* {:vpc-cidr-block     "10.23.0.0/16"
           :subnet-cidr-block  "10.23.1.0/24"
           :security-group     "sg-dd4ef5b8"
           :instance-type      "t2.micro"
           :key-name           "mc-neale-momondo"
           :min-count          3
           :max-count          5
           :tag                "mc-test-2"
           :load-balancer-name "mc-test-elb2"}))

(defn extract-ip-addresses [arch]
  (->> [:reservation :instances]
       (get-in arch)
       (map (juxt :instance-id (comp :private-ip-address first #(filter :primary %) :private-ip-addresses first :network-interfaces)))
       (into {})))
