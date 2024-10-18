$("input:radio").on("click",function (e) {
    var input=$(this);
    if (input.is(".selected-input")) { 
        input.prop("checked",false);
    } else {
        input.prop("checked",true);
    }
    $('#' + searchFormId).submit();
});

$("input:checkbox").on("click",function (e) {
    var input=$(this);
    input.checked = !input.checked;
    $('#' + searchFormId).submit();
});

function clearInput(elementId) {
      let inputEl = document.getElementById(elementId);
      inputEl.value = "";
      let srcButton = document.getElementById("button_" + elementId);
      srcButton.classList.add("unchecked-selected-search-input-label");
      $('#' + searchFormId).submit();
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
        $('#' + searchFormId).submit();
    });
    $(".noUi-handle").on("pointerup", function (e) {
        $('#' + searchFormId).submit();
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
        start: [Number(sliderContainer.querySelector('.range-slider-start').textContent), 
                  Number(sliderContainer.querySelector('.range-slider-end').textContent)],
    
        format: wNumb({
            decimals: 0
        })
    });
    
    var dateValues = [
         sliderContainer.querySelector('.range-slider-start'),
         sliderContainer.querySelector('.range-slider-end')
    ];
    
    var input = sliderContainer.querySelector('.range-slider-input');
    var first = true;
    
    rangeSlider.noUiSlider.on('update', function (values, handle) {
        dateValues[handle].innerHTML = values[handle];
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
}

window.onload = (event) => {
      createSliders();
};

$('#' + searchFormId).submit(function () {
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
