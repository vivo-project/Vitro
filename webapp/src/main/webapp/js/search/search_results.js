$("input:radio").on("click",function (e) {
    var input=$(this);
    if (input.is(".selected-input")) { 
        input.prop("checked",false);
    } else {
        input.prop("checked",true);
    }
    $('#' + searchFormId).trigger("submit");
});

$("input:checkbox").on("click",function (e) {
    var input=$(this);
    input.checked = !input.checked;
    $('#' + searchFormId).trigger("submit");
});

function clearInput(event, elementId) {
      let inputElements = document.querySelectorAll('input[id='+ elementId + ']');
      if (inputElements.length == 0){
          inputElements = document.querySelectorAll('input[name=' + elementId + ']');
          if (inputElements.length == 0){
              return;
          }
      }
      let inputEl = inputElements[0];
      inputEl.value = "";
      event.target.classList.add("unchecked-selected-search-input-label");
      $('#' + searchFormId).trigger("submit");
}

function createSliders(){
    sliders = document.getElementsByClassName('range-slider-container');
    for (let sliderElement of sliders) {
        createSlider(sliderElement);
    }
    $(".noUi-handle").on("mousedown", function (e) {
        $(this)[0].setPointerCapture(e.pointerId);
    });
    $(".noUi-handle").on("mouseup", function (e) {
        $('#' + searchFormId).trigger("submit");
    });
    $(".noUi-handle").on("pointerup", function (e) {
        $('#' + searchFormId).trigger("submit");
    });
};
    
function createSlider(sliderContainer){
    rangeSlider = sliderContainer.querySelector('.range-slider');
    
    noUiSlider.create(rangeSlider, {
        range: {
            min: Number(sliderContainer.getAttribute('min')),
            max: Number(sliderContainer.getAttribute('max'))
        },
    
        step: 1,
        start: [Number(sliderContainer.querySelector('.range-slider-start').value), 
                  Number(sliderContainer.querySelector('.range-slider-end').value)],
    
        format: wNumb({
            decimals: 0
        })
    });
    
    var inputs = [
         sliderContainer.querySelector('.range-slider-start'),
         sliderContainer.querySelector('.range-slider-end')
    ];
    
    var input = sliderContainer.querySelector('.range-slider-input');
    var first = true;
    
    rangeSlider.noUiSlider.on('update', function (values, handle) {
        inputs[handle].value = values[handle];
        var active = input.getAttribute('active');
        if (active === null){
            input.setAttribute('active', "false");
        } else if (active !== "true"){
            input.setAttribute('active', "true");
        } else {
            var startDate = new Date(+values[0],0,1);
            var endDate = new Date(+values[1],0,1);
            input.value = startDate.toISOString() + " " + endDate.toISOString();
        }
    });

inputs.forEach(function(input, handle) {
        input.addEventListener('change', function() {
            rangeSlider.noUiSlider.setHandle(handle, this.value);
        });
        input.addEventListener('keydown', function(e) {
            var values = rangeSlider.noUiSlider.get();
            var value = Number(values[handle]);
            // [[handle0_down, handle0_up], [handle1_down, handle1_up]]
            var steps = rangeSlider.noUiSlider.steps();
            // [down, up]
            var step = steps[handle];
            var position;
            // 13 is enter,
            // 38 is key up,
            // 40 is key down.
            switch (e.which) {
                case 13:
                    rangeSlider.noUiSlider.setHandle(handle, this.value);
                    $('#' + searchFormId).submit();
                    break;
                case 38:
                    // Get step to go increase slider value (up)
                    position = step[1];
                    // false = no step is set
                    if (position === false) {
                        position = 1;
                    }
                    // null = edge of slider
                    if (position !== null) {
                        rangeSlider.noUiSlider.setHandle(handle, value + position);
                    }
                    break;
                case 40:
                    position = step[0];
                    if (position === false) {
                        position = 1;
                    }
                    if (position !== null) {
                        rangeSlider.noUiSlider.setHandle(handle, value - position);
                    }
                    break;
            }
        });
    });
}

window.onload = (event) => {
      createSliders();
};

$('#' + searchFormId).on("submit", function() {
$('#' + searchFormId)
    .find('input')
    .filter(function () {
        return !this.value;
    })
    .prop('name', '');
});

function expandSearchOptions(){
    $(event.target).parent().children('.additional-search-options').removeClass("hidden-search-option");
    $(event.target).parent().children('.less-facets-link').show();
    $(event.target).hide();
}

function collapseSearchOptions(){
    $(event.target).parent().children('.additional-search-options').addClass("hidden-search-option");
    $(event.target).parent().children('.more-facets-link').show();
    $(event.target).hide();
}

function openTab(event, tabName) {
  let currentTab = document.getElementById(tabName);
  let tabs = currentTab.parentElement.querySelectorAll(':scope > .tab');
  for (let i = 0; i < tabs.length; i++) {
    let tab = tabs[i];
    tab.classList.add('fade');
  }
  let tabElement = event.srcElement.parentElement;
  let srcTabs = tabElement.parentElement.querySelectorAll(':scope > .tab');
  for (let i = 0; i < srcTabs.length; i++) {
    let tab = srcTabs[i];
    tab.classList.remove('active');
  }
  tabElement.classList.add('active');
  currentTab.classList.remove('fade');
}