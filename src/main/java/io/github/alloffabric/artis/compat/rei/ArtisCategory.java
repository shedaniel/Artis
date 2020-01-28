package io.github.alloffabric.artis.compat.rei;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.alloffabric.artis.api.ArtisTableType;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ArtisCategory<R extends Recipe> implements RecipeCategory<ArtisDisplay> {
	private final ArtisTableType artisTableType;

	ArtisCategory(ArtisTableType artisTableType) {
        this.artisTableType = artisTableType;
    }

	@Override
	public Identifier getIdentifier() {
		return artisTableType.getId();
	}

	@Override
	public String getCategoryName() {
		return I18n.translate("rei.category." + artisTableType.getId().getPath());
	}

    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Registry.BLOCK.get(artisTableType.getId()));
    }

    public static int getSlotWithSize(ArtisDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }

	@Override
	public List<Widget> setupDisplay(Supplier<ArtisDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - (getDisplayWidth(recipeDisplaySupplier.get()) / 2) + 17, bounds.getCenterY() - (getDisplayHeight() / 2) + 15);

        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                if (this.isRendering()) {
                    int r = (artisTableType.getColor() & 0xFF0000) >> 16;
                    int g = (artisTableType.getColor() & 0xFF00) >> 8;
                    int b = (artisTableType.getColor() & 0xFF);
                    GlStateManager.color4f(r / 255F, g / 255F, b / 255F, 1.0F);
                    this.minecraft.getTextureManager().bindTexture(new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png"));
                    int x = this.getBounds().x;
                    int y = this.getBounds().y;
                    int width = this.getBounds().width;
                    int height = this.getBounds().height;
                    int textureOffset = this.getBlitOffset();
                    this.blit(x, y, 106, 124 + textureOffset, 4, 4);
                    this.blit(x + width - 4, y, 252, 124 + textureOffset, 4, 4);
                    this.blit(x, y + height - 4, 106, 186 + textureOffset, 4, 4);
                    this.blit(x + width - 4, y + height - 4, 252, 186 + textureOffset, 4, 4);

                    int yy;
                    int thisHeight;
                    for(yy = 4; yy < width - 4; yy += 128) {
                        thisHeight = Math.min(128, width - 4 - yy);
                        this.blit(x + yy, y, 110, 124 + textureOffset, thisHeight, 4);
                        this.blit(x + yy, y + height - 4, 110, 186 + textureOffset, thisHeight, 4);
                    }

                    for(yy = 4; yy < height - 4; yy += 50) {
                        thisHeight = Math.min(50, height - 4 - yy);
                        this.blit(x, y + yy, 106, 128 + textureOffset, 4, thisHeight);
                        this.blit(x + width - 4, y + yy, 252, 128 + textureOffset, 4, thisHeight);
                    }

                    this.fillGradient(x + 4, y + 4, x + width - 4, y + height - 4, artisTableType.getColor(), artisTableType.getColor());
                }
            }
        }));

        List<List<EntryStack>> input = recipeDisplaySupplier.get().getInputEntries();
        List<ColorableEntryWidget> slots = Lists.newArrayList();

        for (int y = 0; y < artisTableType.getHeight(); y++)
            for (int x = 0; x < artisTableType.getWidth(); x++)
                slots.add(ColorableEntryWidget.create(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, artisTableType.getColor()));
        for (int i = 0; i < input.size(); i++) {
            if (recipeDisplaySupplier.get() != null) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(recipeDisplaySupplier.get(), i, artisTableType.getWidth())).entries(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).entries(input.get(i));
        }

        widgets.add(new ColorableRecipeArrowWidget(slots.get(slots.size() - 1).getX() + 24, startPoint.y + (getDisplayHeight() / 2) - 23, artisTableType.getColor(), false));

        widgets.addAll(slots);
        widgets.add(new ColorableEntryWidget(slots.get(slots.size() - 1).getX() + 55, startPoint.y + (getDisplayHeight() / 2) - 22, artisTableType.getColor()).entry(recipeDisplaySupplier.get().getOutputEntries().get(0)));
        widgets.add(new ColorableEntryWidget(slots.get(slots.size() - 1).getX() + 28, startPoint.y + (getDisplayHeight() / 2) - 4, artisTableType.getColor()).entry(EntryStack.create(recipeDisplaySupplier.get().getCatalyst().getMatchingStacksClient()[0])));

        widgets.add(new LabelWidget(slots.get(slots.size() - 1).getX() + 35, startPoint.y + (getDisplayHeight() / 2) + 14, Formatting.RED + "-" + recipeDisplaySupplier.get().getCatalystCost()).centered());

        return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 29 + (artisTableType.getHeight() * 18);
	}

    @Override
    public int getDisplayWidth(ArtisDisplay display) {
        return 90 + (artisTableType.getWidth() * 18);
    }
}