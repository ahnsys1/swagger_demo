package com.swagger.demo.controller;

import com.swagger.demo.model.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item API", description = "Endpoints for managing items")
public class ItemController {

    // In-memory data store for demonstration purposes
    private final Map<Long, Item> itemStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    public ItemController() {
        // Pre-populate with some data
        long id1 = idCounter.incrementAndGet();
        itemStore.put(id1, new Item(id1, "Sample Item 1", "This is the first sample item."));
        long id2 = idCounter.incrementAndGet();
        itemStore.put(id2, new Item(id2, "Sample Item 2", "This is the second sample item."));
    }

    @Operation(summary = "Get all items", description = "Retrieves a list of all items in the store.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Item.class)))
    @GetMapping
    public Collection<Item> getAllItems() {
        return itemStore.values();
    }

    @Operation(summary = "Get an item by ID", description = "Retrieves a single item based on its unique ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the item", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Item.class))}),
        @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(
            @Parameter(description = "ID of the item to be retrieved", required = true) @PathVariable long id) {
        Item item = itemStore.get(id);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new item", description = "Adds a new item to the store.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item created successfully", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Item.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Item> createItem(
            @Parameter(description = "Item object to be created", required = true) @Valid @RequestBody Item item) {
        long newId = idCounter.incrementAndGet();
        item.setId(newId);
        itemStore.put(newId, item);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing item", description = "Updates the details of an existing item by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item updated successfully", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Item.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @Parameter(description = "ID of the item to be updated", required = true) @PathVariable long id,
            @Parameter(description = "Updated item object", required = true) @Valid @RequestBody Item item) {
        if (!itemStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        item.setId(id); // Ensure the ID is correct
        itemStore.put(id, item);
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "Delete an item", description = "Deletes an item from the store by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the item to be deleted", required = true) @PathVariable long id) {
        if (itemStore.remove(id) != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
