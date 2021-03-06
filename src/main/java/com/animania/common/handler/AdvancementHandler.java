package com.animania.common.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.animania.common.advancements.criterion.FeedAnimalTrigger;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class AdvancementHandler
{
	
	public static FeedAnimalTrigger feedAnimal;
	
	
	public static void registerCriteria()
	{
		feedAnimal = (FeedAnimalTrigger) registerTrigger(new FeedAnimalTrigger());
	}
	

	public static <T extends ICriterionInstance> ICriterionTrigger<T> registerTrigger(ICriterionTrigger<T> trigger)
	{
		Method method;

		method = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class);
		method.setAccessible(true);

		try
		{
			trigger = (ICriterionTrigger<T>) method.invoke(null, trigger);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			System.out.println("Failed to register trigger " + trigger.getId() + "!");
			e.printStackTrace();
		}
		return trigger;
	}

}
