(define (problem webshop1)
(:domain webshop)
(:objects
		cpu hdd gpu display motherboard ram dvd - type
		ojb_3GB1600MHzDDR3RAMGeilValueKitSPD83x1GB obj_AMDPhenomIIX2550dobozosSocketAM3BlackEdition obj_1GB800MHzDDR2RAMGeilValueSPD5 obj_15TBSamsungHD154UISATAII32MBcachewinchesterECOgreen - product
		zold piros - shop)
(:init
	(prod obj_1GB800MHzDDR2RAMGeilValueSPD5  ram zold)
	(prod ojb_3GB1600MHzDDR3RAMGeilValueKitSPD83x1GB ram zold)
	(prod obj_AMDPhenomIIX2550dobozosSocketAM3BlackEdition hdd piros)
	(prod obj_15TBSamsungHD154UISATAII32MBcachewinchesterECOgreen  hdd piros)
	
	(compat obj_1GB800MHzDDR2RAMGeilValueSPD5 obj_15TBSamsungHD154UISATAII32MBcachewinchesterECOgreen)
	(compat ojb_3GB1600MHzDDR3RAMGeilValueKitSPD83x1GB obj_AMDPhenomIIX2550dobozosSocketAM3BlackEdition)
	
	(= (price obj_1GB800MHzDDR2RAMGeilValueSPD5) 1)
	(= (price ojb_3GB1600MHzDDR3RAMGeilValueKitSPD83x1GB) 1)
	(= (price obj_AMDPhenomIIX2550dobozosSocketAM3BlackEdition) 1)
	(= (price obj_15TBSamsungHD154UISATAII32MBcachewinchesterECOgreen) 0)
	
	(= (reliability obj_1GB800MHzDDR2RAMGeilValueSPD5) 1)
	(= (reliability ojb_3GB1600MHzDDR3RAMGeilValueKitSPD83x1GB) 1)
	(= (reliability obj_AMDPhenomIIX2550dobozosSocketAM3BlackEdition) 1)
	(= (reliability obj_15TBSamsungHD154UISATAII32MBcachewinchesterECOgreen) 0)
	
	(= (total_cost) 0)
	(= (remaining_cash) 1)
	(= (total_reliability) 0)
)
(:goal 	(and	(in_cart_type ram)
					(in_cart_type hdd)
					(compat_in_cart hdd ram)
					(forall (?s - shop) (and 	(shopped_from ?s)
														(checked_out ?s)
											))
			)
)

(:metric maximize (total_reliability))

)
